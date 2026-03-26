param(
  [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
  [string]$AdbPath = "",
  [switch]$SkipLaunch
)

$ErrorActionPreference = "Stop"
$packageName = "com.cyberlist.neonlist"
$androidDir = Join-Path $ProjectRoot "android"

function Resolve-AdbPath {
  param(
    [string]$BaseDir,
    [string]$ExplicitAdbPath
  )

  if ($ExplicitAdbPath -and (Test-Path $ExplicitAdbPath)) {
    return (Resolve-Path $ExplicitAdbPath).Path
  }

  $localPropsPath = Join-Path $BaseDir "local.properties"
  if (Test-Path $localPropsPath) {
    $sdkLine = (Get-Content $localPropsPath | Where-Object { $_ -like "sdk.dir=*" } | Select-Object -First 1)
    if ($sdkLine) {
      $escaped = $sdkLine.Substring("sdk.dir=".Length)
      $sdkDir = $escaped.Replace("\:", ":").Replace("\\", "\")
      $candidate = Join-Path $sdkDir "platform-tools\adb.exe"
      if (Test-Path $candidate) {
        return (Resolve-Path $candidate).Path
      }
    }
  }

  $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
  if ($adbCommand) {
    return $adbCommand.Source
  }

  throw "Could not resolve adb path. Provide -AdbPath explicitly."
}

$resolvedAdb = Resolve-AdbPath -BaseDir $androidDir -ExplicitAdbPath $AdbPath
Write-Output "Using adb: $resolvedAdb"

Push-Location $androidDir
try {
  Write-Output "==> Build debug APK"
  ./gradlew :app:assembleDebug

  Write-Output "==> Uninstall existing package (ignore non-zero)"
  & $resolvedAdb uninstall $packageName
  if ($LASTEXITCODE -ne 0) {
    Write-Output "adb uninstall returned $LASTEXITCODE, continuing."
  }

  Write-Output "==> Install fresh debug APK"
  ./gradlew :app:installDebug

  Write-Output "==> Verify installed package path"
  & $resolvedAdb shell pm path $packageName

  Write-Output "==> Verify versionCode/versionName"
  & $resolvedAdb shell dumpsys package $packageName | Select-String -Pattern "versionCode|versionName"

  if (-not $SkipLaunch) {
    Write-Output "==> Launch app"
    & $resolvedAdb shell monkey -p $packageName -c android.intent.category.LAUNCHER 1
  }
} finally {
  Pop-Location
}
