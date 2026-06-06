# 💰 Android Budget Tracker

Welcome to the **Budget Tracker** app! This is a fully offline, privacy-first, premium Android application designed to help you master your personal finances. 

Whether you're looking to monitor your daily expenses, set strict budget limits, or achieve long-term savings goals using the 50/30/20 rule, this app has you covered!

---

## 🌟 Key Features

1. **Local-First Architecture (Offline Mode)**
   - **Privacy Guaranteed**: Your financial data never leaves your device. Everything is securely stored in a local SQLite database (via Room). 
   - No cloud syncing, no accounts, no internet required.

2. **Advanced Budgeting & Strict Limits**
   - **50/30/20 Rule Integration**: Expenses are optionally categorized into Needs, Wants, and Savings to help you visually track your goals.
   - **Strict Caps**: Set a fixed budget for a specific category (e.g., ₱2,000 for Dining Out). If you try to log a transaction that exceeds this, the app actively blocks you from overspending!

3. **Receipt Scanning (OCR)**
   - **Offline Text Recognition**: Powered by Google's ML Kit, you can snap a photo or pick a receipt from your gallery. The app automatically extracts text and attempts to auto-fill the transaction note and amount for you.

4. **Recurring Transactions Automation**
   - Got a Netflix subscription or a monthly rent payment? Set up a recurring transaction (Daily, Weekly, Monthly, or Yearly), and Android's `WorkManager` will quietly log it for you in the background when it's due.

5. **Full Ledger Management**
   - **Swipe-to-Delete**: Quickly remove mistakes from the Transactions screen with intuitive gestures.
   - **Tap-to-Edit**: Select any transaction to reopen the edit screen. The database atomically recalculates your wallet balances when you update an old transaction.
   - **Global Search & Filter**: Easily search transactions by name, or filter them by Date Range, Wallet Account, or Category.

6. **JSON Backup & Restore**
   - Securely export your entire database (Accounts, Categories, Limits, and Transactions) to a JSON file anywhere on your phone using Android's Storage Access Framework. Import it back later to restore your data!

7. **Interactive Analytics**
   - **Visual Breakdown**: Beautiful, dynamic pie charts instantly break down your spending by category.
   - **50/30/20 Progress**: A dedicated view showing exactly how much of your total income has been allocated to Needs, Wants, and Savings using interactive progress bars.

8. **Savings Goals Tracker**
   - Set long-term financial targets (e.g., "Emergency Fund", "New Car") with specific target amounts and deadlines. Track your exact progress towards your goal over time.

9. **Premium OLED Dark Mode UI**
   - Built with modern Jetpack Compose Material 3 standards. Features True Black backgrounds for battery-saving OLED screens, dynamic color themes, and gorgeous micro-animations.

---

## 📖 How to Use (Tutorials & Examples)

### 1. Setting Up Your Wallets and Categories
Before logging expenses, you need a wallet (account) to pull money from.
- **Go to:** Settings -> Data Management -> Wallets.
- **Action:** Add a new wallet (e.g., "Cash", "BPI Debit", "GCash").
- *Note:* The app comes pre-seeded with default categories like Food, Utilities, and Salary, but you can add custom categories under Settings -> Data Management -> Categories.

### 2. Logging a Standard Income or Expense
- **Go to:** The Home Screen and tap the large **"+"** (Add Transaction) button.
- **Action:** 
  1. Enter the amount.
  2. Select whether it's an **Expense** or **Income**.
  3. Pick the Wallet it came from and the Category it belongs to.
  4. (Optional) For expenses, tag it as a **Need**, **Want**, or **Saving**.
  5. Tap **Save**. Your Home screen wallet balance will immediately update!

### 3. Using the OCR Receipt Scanner
Don't want to type the amount? Let the AI do it!
- **Go to:** The Add Transaction Screen.
- **Action:** Tap the **Camera Icon** in the top right corner.
- Choose either your Gallery or Camera. Snap a photo of a clear receipt. 
- The app will scan the receipt entirely offline, pull the total amount, and paste the store's name into the Note section!

### 4. Setting Up a Monthly Subscription (Recurring)
- **Go to:** Settings -> Data Management -> Recurring Transactions.
- **Action:** Tap "Add Recurring Transaction".
- **Example:** You pay ₱500 for Netflix every month.
  - Amount: 500
  - Note: "Netflix Subscription"
  - Frequency: Monthly
  - Category: Entertainment
- The app will automatically log a ₱500 expense exactly 1 month from today, in the background.

### 5. Enforcing a Strict Budget Limit
Want to stop yourself from spending too much on Coffee?
- **Go to:** The Home Screen and tap **"Assign Budget"**.
- **Action:** Find the "Coffee" category and assign a budget limit of ₱1,000 for the current month.
- **Result:** If your total coffee expenses hit ₱1,000, the app will display a warning dialog and block you from saving any further coffee transactions until the next month!

### 6. Editing a Mistake
- **Go to:** The Transactions (Ledger) Tab.
- **Action:** Tap on any transaction card you want to fix. 
- You'll be taken to the Edit Screen where you can fix the amount or category. When you hit Save, the app automatically refunds your wallet from the old mistake and deducts the correct amount!

### 7. Tracking Savings Goals
- **Go to:** The Goals Tab (Star icon).
- **Action:** Tap the large "+" button to create a new savings goal.
- **Example:** You want to save ₱50,000 for a "Vacation Fund" by December. Enter the target and deadline. As you set money aside in real life, tap your goal and "Add Progress" to see the visual tracker fill up!

### 8. Viewing Your Analytics
Wondering where all your money went this month?
- **Go to:** The Analytics Tab (Pie Chart icon).
- **Action:** You'll immediately see a Pie Chart breaking down your expenses by Category (e.g., 40% Food, 30% Rent). 
- **Bonus:** Swipe to the "50/30/20 Breakdown" tab to see if you are adhering to your budget buckets. It automatically calculates your total income for the month and tells you if you are overspending on your "Wants"!

### 9. Exporting Your Data
- **Go to:** Settings -> Data Management -> Export Data.
- **Action:** Tap Export. A file picker will appear.
- Save the `budget_backup.json` file to your Google Drive folder or Downloads folder. If you ever switch phones, just install the app, tap "Import Data", and select that JSON file to instantly restore your entire ledger!

---

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Local Database**: Room DB (SQLite) & KSP
- **Background Tasks**: WorkManager
- **Machine Learning**: Google ML Kit (Text Recognition v2)
- **Architecture**: MVVM with StateFlow & Coroutines
- **Dependency Injection**: Manual DI via AppContainer
