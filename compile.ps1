param(
    [string]$OutputDir = "target\classes",
    [string]$JarOutput = "target\OmniRepair-1.0.0-SNAPSHOT.jar"
)

# Find Java
$javaExe = (Get-ChildItem 'C:\Program Files' -Recurse -Filter 'javac.exe' -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
if (-not $javaExe) {
    Write-Error "Java compiler not found!"
    exit 1
}

$javaDir = (Get-Item $javaExe).Directory.Parent.FullName
Write-Host "Using Java: $javaDir"

# Create output directory
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

# Download dependencies to lib folder
$libDir = "target\lib"
New-Item -ItemType Directory -Force -Path $libDir | Out-Null

# Paper API (using a known working version)
$paperUrl = "https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/1.20.6-R0.1-SNAPSHOT/paper-api-1.20.6-R0.1-20240613.153838-1.jar"
$paperPath = Join-Path $libDir "paper-api.jar"
if (-not (Test-Path $paperPath)) {
    Write-Host "Downloading Paper API..."
    try {
        Invoke-WebRequest -Uri $paperUrl -OutFile $paperPath -UseBasicParsing
        Write-Host "  Downloaded: paper-api.jar"
    } catch {
        Write-Host "  Failed: $($_.Exception.Message)"
    }
}

# Spigot API as fallback
$spigotUrl = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/1.21-R0.1-SNAPSHOT/spigot-api-1.21-R0.1-20240613.153838-1.jar"
$spigotPath = Join-Path $libDir "spigot-api.jar"
if (-not (Test-Path $spigotPath)) {
    Write-Host "Downloading Spigot API..."
    try {
        Invoke-WebRequest -Uri $spigotUrl -OutFile $spigotPath -UseBasicParsing
        Write-Host "  Downloaded: spigot-api.jar"
    } catch {
        Write-Host "  Failed: $($_.Exception.Message)"
    }
}

# Vault API
$vaultUrl = "https://jitpack.io/com/github/MilkBowl/VaultAPI/1.7.1/VaultAPI-1.7.1.jar"
$vaultPath = Join-Path $libDir "VaultAPI.jar"
if (-not (Test-Path $vaultPath)) {
    Write-Host "Downloading Vault API..."
    try {
        Invoke-WebRequest -Uri $vaultUrl -OutFile $vaultPath -UseBasicParsing
        Write-Host "  Downloaded: VaultAPI.jar"
    } catch {
        Write-Host "  Failed: $($_.Exception.Message)"
    }
}

# Get all jar files for classpath
$jarFiles = Get-ChildItem $libDir -Filter *.jar -ErrorAction SilentlyContinue
if ($jarFiles.Count -gt 0) {
    $classpath = [string]::Join(";", ($jarFiles | ForEach-Object { $_.FullName }))
    Write-Host "Classpath: $classpath"
} else {
    Write-Error "No dependencies found! Cannot compile."
    exit 1
}

# Find all Java files
$javaFiles = Get-ChildItem "src\main\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
Write-Host "Compiling $($javaFiles.Count) Java files..."

# Compile
$javacArgs = @("-d", $OutputDir, "-cp", $classpath, "-encoding", "UTF-8", "-source", "21", "-target", "21") + $javaFiles

& $javaExe $javacArgs 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
    
    # Copy resources
    if (Test-Path "src\main\resources") {
        Copy-Item -Recurse -Force "src\main\resources\*" $OutputDir
    }
    
    # Create JAR
    Write-Host "Creating JAR..."
    & "$javaDir\bin\jar.exe" cvf $JarOutput -C $OutputDir .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "==========================================" -ForegroundColor Green
        Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
        Write-Host "==========================================" -ForegroundColor Green
        Write-Host "Output: $JarOutput"
    } else {
        Write-Error "Failed to create JAR!"
    }
} else {
    Write-Error "Compilation failed!"
    exit 1
}
