# Build script for OmniRepair
$JAVA_HOME = (Get-ChildItem 'C:\Program Files' -Recurse -Filter 'javac.exe' -ErrorAction SilentlyContinue | Select-Object -First 1).Directory.Parent.FullName
$env:JAVA_HOME = $JAVA_HOME

Write-Host "Using Java: $JAVA_HOME"
Write-Host ""

# Check if Maven wrapper exists
if (Test-Path ".\mvnw.cmd") {
    Write-Host "Building with Maven wrapper..."
    & ".\mvnw.cmd" clean package -DskipTests
} else {
    Write-Host "Maven wrapper not found. Please install Maven or use IntelliJ IDEA to build."
    exit 1
}

# Check if build was successful
if (Test-Path ".\target\OmniRepair-1.0.0-SNAPSHOT.jar") {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Output: target\OmniRepair-1.0.0-SNAPSHOT.jar"
} else {
    Write-Host ""
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    exit 1
}
