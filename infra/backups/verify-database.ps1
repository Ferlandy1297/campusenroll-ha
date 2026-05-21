[CmdletBinding()]
param(
    [string]$ContainerName = 'campusenroll-postgres',
    [string]$Database = 'campusenroll',
    [string]$User = 'campus',
    [string]$Password = 'campus_password'
)

$ErrorActionPreference = 'Stop'

function Invoke-ScalarQuery {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Query
    )

    $result = (& docker exec -e "PGPASSWORD=$Password" $ContainerName psql -U $User -d $Database -v ON_ERROR_STOP=1 -t -A -c $Query | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw "Database query failed: $Query"
    }

    return $result
}

try {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker CLI was not found in PATH."
    }

    $runningState = (& docker inspect -f '{{.State.Running}}' $ContainerName 2>$null | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $runningState -ne 'true') {
        throw "Container '$ContainerName' is not running."
    }

    Write-Host "Current database connection:"
    & docker exec -e "PGPASSWORD=$Password" $ContainerName psql -U $User -d $Database -v ON_ERROR_STOP=1 -c "SELECT current_database() AS database_name, current_user AS database_user;"
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to query current database or current user."
    }

    $importantTables = @('students', 'courses', 'sections', 'enrollments', 'billings')
    foreach ($tableName in $importantTables) {
        $exists = Invoke-ScalarQuery -Query "SELECT to_regclass('public.$tableName') IS NOT NULL;"
        if ($exists -eq 't') {
            $rowCount = Invoke-ScalarQuery -Query "SELECT COUNT(*) FROM public.$tableName;"
            Write-Host ("Table {0}: {1} rows" -f $tableName, $rowCount)
        }
        else {
            Write-Host ("Table {0}: not found" -f $tableName)
        }
    }
}
catch {
    Write-Error "Verification failed: $($_.Exception.Message)"
    exit 1
}
