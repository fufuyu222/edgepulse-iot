$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = Join-Path $root "tools\jdk-17\jdk-17.0.19+10"
$mavenBin = Join-Path $root "tools\maven\apache-maven-3.9.16\bin"
$env:Path = "$env:JAVA_HOME\bin;$mavenBin;$env:Path"

Set-Location (Join-Path $root "backend")
& (Join-Path $mavenBin "mvn.cmd") spring-boot:run
