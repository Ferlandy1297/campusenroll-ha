[CmdletBinding()]
param(
    [string]$ContainerName = 'campusenroll-postgres',
    [string]$Database = 'campusenroll',
    [string]$User = 'campus',
    [string]$Password = 'campus_password',
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $scriptDirectory 'output'
}

function Resolve-OutputDirectory {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }

    return [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $Path))
}

try {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker CLI was not found in PATH."
    }

    $resolvedOutputDirectory = Resolve-OutputDirectory -Path $OutputDirectory
    New-Item -ItemType Directory -Path $resolvedOutputDirectory -Force | Out-Null

    $runningState = (& docker inspect -f '{{.State.Running}}' $ContainerName 2>$null | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $runningState -ne 'true') {
        throw "Container '$ContainerName' is not running."
    }

    $timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $backupFileName = '{0}-{1}.dump' -f $Database, $timestamp
    $containerBackupPath = "/tmp/$backupFileName"
    $localBackupPath = Join-Path $resolvedOutputDirectory $backupFileName

    Write-Host "Verifying PostgreSQL connectivity in container '$ContainerName'..."
    & docker exec -e "PGPASSWORD=$Password" $ContainerName psql -U $User -d $Database -v ON_ERROR_STOP=1 -c "SELECT current_database() AS database_name, current_user AS database_user;"
    if ($LASTEXITCODE -ne 0) {
        throw "Database connectivity check failed before backup."
    }

    Write-Host "Creating backup file '$backupFileName' inside the container..."
    & docker exec -e "PGPASSWORD=$Password" $ContainerName pg_dump -U $User -d $Database -F c -f $containerBackupPath
    if ($LASTEXITCODE -ne 0) {
        throw "pg_dump failed inside container '$ContainerName'."
    }

    Write-Host "Copying backup to '$localBackupPath'..."
    & docker cp "${ContainerName}:$containerBackupPath" $localBackupPath
    if ($LASTEXITCODE -ne 0) {
        throw "docker cp failed while copying the backup to the host."
    }

    if (-not (Test-Path -LiteralPath $localBackupPath -PathType Leaf)) {
        throw "Backup file was not created at '$localBackupPath'."
    }

    $backupFile = Get-Item -LiteralPath $localBackupPath
    if ($backupFile.Length -le 0) {
        throw "Backup file '$localBackupPath' is empty."
    }

    Write-Host "Backup created successfully."
    Write-Host "File: $($backupFile.FullName)"
    Write-Host ("Size: {0} bytes" -f $backupFile.Length)
    exit 0
}
catch {
    Write-Error "Backup failed: $($_.Exception.Message)"
    exit 1
}
finally {
    if ($containerBackupPath) {
        & docker exec $ContainerName rm -f $containerBackupPath *> $null
    }
}
