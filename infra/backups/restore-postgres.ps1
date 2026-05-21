[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,
    [string]$ContainerName = 'campusenroll-postgres',
    [string]$Database = 'campusenroll',
    [string]$User = 'campus',
    [string]$Password = 'campus_password',
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path

function Resolve-ExistingFilePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $candidatePaths = @()
    if ([System.IO.Path]::IsPathRooted($Path)) {
        $candidatePaths += [System.IO.Path]::GetFullPath($Path)
    }
    else {
        $repoRoot = Split-Path (Split-Path $scriptDirectory -Parent) -Parent
        $candidatePaths += [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $Path))
        $candidatePaths += [System.IO.Path]::GetFullPath((Join-Path $scriptDirectory $Path))
        $candidatePaths += [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Path))
    }

    foreach ($candidatePath in ($candidatePaths | Select-Object -Unique)) {
        if (Test-Path -LiteralPath $candidatePath -PathType Leaf) {
            return $candidatePath
        }
    }

    throw "Backup file '$Path' was not found."
}

function Invoke-DatabaseQuery {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Query
    )

    & docker exec -e "PGPASSWORD=$Password" $ContainerName psql -U $User -d $Database -v ON_ERROR_STOP=1 -c $Query
    if ($LASTEXITCODE -ne 0) {
        throw "Database query failed: $Query"
    }
}

$containerBackupPath = $null

try {
    if (-not $Force) {
        Write-Warning "Restore will overwrite current objects in database '$Database'. Re-run with -Force to continue."
        exit 1
    }

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker CLI was not found in PATH."
    }

    $resolvedBackupFile = Resolve-ExistingFilePath -Path $BackupFile
    $backupInfo = Get-Item -LiteralPath $resolvedBackupFile
    if ($backupInfo.Length -le 0) {
        throw "Backup file '$resolvedBackupFile' is empty."
    }

    $runningState = (& docker inspect -f '{{.State.Running}}' $ContainerName 2>$null | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $runningState -ne 'true') {
        throw "Container '$ContainerName' is not running."
    }

    Write-Warning "Restoring '$($backupInfo.FullName)' into '$Database' on '$ContainerName'."
    Write-Warning "Recommended: stop application services or local Spring Boot processes before restore to avoid concurrent writes."

    Write-Host "Verifying database connectivity before restore..."
    Invoke-DatabaseQuery -Query "SELECT current_database() AS database_name, current_user AS database_user;"

    $containerBackupPath = "/tmp/$($backupInfo.Name)"
    Write-Host "Copying backup into container path '$containerBackupPath'..."
    & docker cp $backupInfo.FullName "${ContainerName}:$containerBackupPath"
    if ($LASTEXITCODE -ne 0) {
        throw "docker cp failed while sending the backup into the container."
    }

    Write-Host "Restoring backup with pg_restore --clean --if-exists..."
    & docker exec -e "PGPASSWORD=$Password" $ContainerName pg_restore --clean --if-exists --no-owner --no-privileges -U $User -d $Database $containerBackupPath
    if ($LASTEXITCODE -ne 0) {
        throw "pg_restore failed."
    }

    Write-Host "Running post-restore verification query..."
    Invoke-DatabaseQuery -Query "SELECT COUNT(*) AS public_table_count FROM information_schema.tables WHERE table_schema = 'public';"

    Write-Host "Restore completed successfully."
    exit 0
}
catch {
    Write-Error "Restore failed: $($_.Exception.Message)"
    exit 1
}
finally {
    if ($containerBackupPath) {
        & docker exec $ContainerName rm -f $containerBackupPath *> $null
    }
}
