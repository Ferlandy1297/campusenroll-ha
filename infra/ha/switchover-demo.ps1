[CmdletBinding()]
param(
    [string]$GatewayBaseUrl = 'http://localhost:8080',
    [string]$StatsUrl = 'http://localhost:8404/stats',
    [int]$HealthCheckDelaySeconds = 8,
    [switch]$OpenStats
)

$ErrorActionPreference = 'Stop'
$composeFiles = @(
    '-f', 'docker-compose.yml',
    '-f', 'docker-compose.apps.yml',
    '-f', 'docker-compose.ha-demo.yml'
)

function Invoke-Compose {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & docker compose @composeFiles @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose $($Arguments -join ' ') failed."
    }
}

function Test-Http200 {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [int]$TimeoutSec = 20
    )

    $response = Invoke-WebRequest -Uri $Url -Method Get -UseBasicParsing -TimeoutSec $TimeoutSec
    if ([int]$response.StatusCode -ne 200) {
        throw "Expected HTTP 200 from $Url but received $($response.StatusCode)."
    }

    Write-Host ("HTTP {0} from {1}" -f [int]$response.StatusCode, $Url)
}

$primaryStopped = $false
$primaryRestarted = $false
$gatewayCatalogUrl = '{0}/api/courses' -f $GatewayBaseUrl.TrimEnd('/')
$gatewayHealthUrl = '{0}/health/course' -f $GatewayBaseUrl.TrimEnd('/')

try {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker CLI was not found in PATH."
    }

    Write-Host "Checking HAProxy gateway before planned switchover..."
    Test-Http200 -Url $gatewayCatalogUrl
    Test-Http200 -Url $gatewayHealthUrl

    if ($OpenStats) {
        Start-Process $StatsUrl
    }

    Write-Host "Stopping primary course-service for planned maintenance..."
    Invoke-Compose -Arguments @('stop', 'course-service')
    $primaryStopped = $true

    Start-Sleep -Seconds $HealthCheckDelaySeconds

    Write-Host "Checking HAProxy gateway while the replica carries traffic..."
    Test-Http200 -Url $gatewayCatalogUrl
    Test-Http200 -Url $gatewayHealthUrl
    Write-Host "This is a planned switchover: traffic stays available while the primary is intentionally out of rotation."

    Write-Host "Starting primary course-service again..."
    Invoke-Compose -Arguments @('start', 'course-service')
    $primaryRestarted = $true

    Start-Sleep -Seconds 5

    Write-Host "Checking HAProxy gateway after the primary returns..."
    Test-Http200 -Url $gatewayCatalogUrl
    Test-Http200 -Url $gatewayHealthUrl
    Write-Host ("HAProxy stats: {0}" -f $StatsUrl)
}
catch {
    $errorMessage = $_.Exception.Message
    Write-Error "Switchover demo failed: $errorMessage"
    exit 1
}
finally {
    if ($primaryStopped -and -not $primaryRestarted) {
        try {
            Write-Host "Starting primary course-service as cleanup..."
            Invoke-Compose -Arguments @('start', 'course-service')
        }
        catch {
            Write-Warning "Cleanup could not restart course-service: $($_.Exception.Message)"
        }
    }
}
