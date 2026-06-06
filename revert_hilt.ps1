$viewModels = Get-ChildItem -Path "c:\Users\DJ\AndroidStudioProjects\BudgetTracker\app\src\main\java\com\example\budgettracker\ui" -Recurse -Filter "*ViewModel.kt"

foreach ($file in $viewModels) {
    $content = Get-Content $file.FullName
    $content = $content -replace "import dagger\.hilt\.android\.lifecycle\.HiltViewModel", ""
    $content = $content -replace "import javax\.inject\.Inject", ""
    $content = $content -replace "@HiltViewModel", ""
    $content = $content -replace "class (.*) @Inject constructor\(", "class `$1("
    Set-Content -Path $file.FullName -Value $content
}
