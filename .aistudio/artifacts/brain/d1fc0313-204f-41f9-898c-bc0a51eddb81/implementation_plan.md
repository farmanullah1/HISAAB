# Hisaab do Farmanullah Ansari — Master Implementation Plan
## Production-Grade Android Personal Finance, Automation, Data Integrity & Quality Engineering

A dependable, offline-first native Android personal finance application built with Kotlin, Jetpack Compose, Material 3, and Room SQLite. Engineered strictly according to the **Anti-AI-Slop Financial Contract**, Hisaab guarantees mathematical precision in Pakistani Rupees (PKR), authoritative local persistence, atomic multi-account transfers, manual statement reconciliation, explicit recurring bill confirmations, app-private receipt storage, and user-controlled local backup and restore.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Anti-AI-Slop Financial Contract is Fully Enforced**:
> - **Zero Fabricated Data**: No fake balances, no sample transactions seeded into production, no synthetic history, and no hardcoded dashboard totals. An empty database shows exactly `Rs. 0.00` and guides the user to add real accounts or opening balances.
> - **Zero Fake Controls**: Every button, form, filter, search query, backup tool, and export action executes real database and file system operations.
> - **Exact Integer Money**: Monetary values are stored and calculated strictly as 64-bit integer minor units (`paisa`: Long) to eradicate floating-point rounding errors.
> - **Zero Unconfirmed Actions**: Recurring occurrences are tracked as reminders and NEVER booked into ledger accounts until explicitly confirmed by the user.

- **Confirmed Application Name**: `"Hisaab do Farmanullah Ansari"` (set in `metadata.json` and `res/values/strings.xml`).
- **Confirmed Currency**: Pakistani Rupee (`PKR`), displayed as `Rs. #,##0.00`.
- **Confirmed Visual Direction**:
  - **Hero Balance Card**: Linear gradient brush from Coral (`#FF6B6B`) to Yellow (`#FFD93D`).
  - **Canvas & Background**: Soft warm cream (`#FFF9F5`).
  - **Surface & Cards**: Pure white (`#FFFFFF`) with soft borders (`#FFF3E0`) and 16dp rounded corners.
  - **Typography**: Deep charcoal primary text (`#2D2A26`), medium gray secondary text (`#6B6B6B`).
  - **Category Color Accents**: Food Rose (`#FF6B6B`), Travel Blue (`#3B82F6`), Shopping Emerald (`#10B981`).
- **Confirmed Initial Accounts**: 3 standard starter accounts (**Cash in Hand**, **Bank Account**, **Mobile Wallet**) created on first database setup with 0 opening balance (no fake sample transactions).
- **Confirmed Offline-First Architecture**: Strictly local-only using Android Room SQLite, DataStore, and internal file storage. Zero network telemetry or remote servers.

---

## 1. Overview & Core Concept

- **What It Does**: Hisaab acts as a private, dependable ledger on the user's phone. It automates financial arithmetic, category budget tracking, upcoming payment reminders, and integrity diagnostics while preserving absolute user authority over every financial event.
- **Target Persona**: Individuals, household managers, and professionals who demand exactness in their daily expense tracking and value strict privacy and offline reliability.
- **Key Value**: Deterministic balance calculations, total offline security, zero ads, zero subscriptions, and complete ownership of private financial records and receipt images.

---

## 2. User Experience & Visual Design

### 2.1 Screen Architecture & User Journeys

1. **Dashboard & Executive Overview**:
   - **Hero Spending Card**: Displays Total Available Net Balance in large high-contrast typography over the custom `#FF6B6B` to `#FFD93D` coral-yellow gradient. Shows current-month Income and Expenses calculated strictly from authoritative database records.
   - **Starter Account Carousel**: Quick balance pills for Cash, Bank, and Mobile Wallet.
   - **Upcoming Bills Warning Strip**: Highlights scheduled recurring payments due today or overdue.
   - **Recent Transactions Ledger**: Displays the latest real transactions with category icons, account tags, and receipt indicators. Displays a genuine empty state when no transactions exist.
2. **Transaction Management & Fast Entry**:
   - Primary creation FAB launches an interactive bottom sheet with immediate numeric keypad focus.
   - Toggle: **Expense**, **Income**, or **Transfer**.
   - Input fields: Exact amount (minor unit validation), Account selector, Category chips, Date selector (defaults to current date, supports historical dates), Merchant/Payee, Description, and Receipt attachment launcher.
   - **Real-Time Duplicate Warning**: If an identical amount, account, and merchant occurred within 24 hours, displays a non-blocking warning badge allowing the user to review or proceed.
   - **Undo Action**: Deleting a transaction provides a temporary snackbar with an Undo action that restores the record and its receipt relationship.
3. **Multi-Account Hub & Atomic Transfers**:
   - List of all active accounts with calculated balances, opening balances, and account type icons (Cash, Bank, Mobile Wallet, Savings).
   - **Atomic Transfer Flow**: Moves funds from Account A to Account B. Validates sufficient balance, updates both accounts in a single Room database transaction, and ensures total wealth is invariant.
   - **Account Reconciliation Tool**: Allows entering a real bank statement or cash count balance. Calculates `Difference = Calculated - Statement`. Offers choices to record an explicit adjustment transaction, edit an entry, or leave unresolved.
4. **Budgets & Spending Analysis**:
   - Monthly and Category-specific budget limits.
   - Dynamic progress bars with color-coded threshold states: Normal (<50%), Warning (50%–80%), Danger (80%–100%), and Over-budget (>100%).
   - Spend breakdown by category and 6-month historical trend.
5. **Recurring Bills & Payment Reminders**:
   - Scheduled rules (Daily, Weekly, Monthly, Yearly).
   - Occurrence cards with clear status tags: *Upcoming*, *Due Today*, *Overdue Reminder*.
   - Actions per occurrence: **Confirm & Book**, **Edit Before Confirming**, **Skip**, or **Postpone**.
6. **Receipt Attachment Vault**:
   - Attach photos via Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
   - Photos copied to app-private internal directory with UUID identifiers.
   - In-app receipt viewer dialog with zoom and delete/replace capability.
7. **Backup, Restore & Data Diagnostics**:
   - **JSON / CSV Export**: Exports complete relational dataset and manifest.
   - **Verified Restore**: Validates manifest schema, integrity, and foreign keys before prompting the user for replacement confirmation.
   - **Integrity Diagnostic**: Scans for orphaned attachments, dangling category/account references, or balance discrepancies.

### 2.2 Design Tokens & Visual Hierarchy

- **Color Palette**:
  - `GradientHeroStart`: `#FF6B6B` (Coral)
  - `GradientHeroEnd`: `#FFD93D` (Warm Yellow)
  - `Background`: `#FFF9F5` (Soft warm cream)
  - `SurfaceCard`: `#FFFFFF` (Pure white)
  - `CardBorder`: `#FFF3E0` (Subtle warm amber)
  - `TextPrimary`: `#2D2A26` (Deep charcoal)
  - `TextSecondary`: `#6B6B6B` (Medium neutral gray)
  - `CategoryFood`: `#FF6B6B` (Rose/Pink)
  - `CategoryTravel`: `#3B82F6` (Electric Blue)
  - `CategoryShopping`: `#10B981` (Emerald Green)
  - `CategoryBills`: `#F59E0B` (Amber)
  - `CategorySalary`: `#10B981` (Income Emerald)
- **Component Geometry & Insets**:
  - 16dp rounded corners for standard cards, 24dp for modal bottom sheets.
  - Generous 16dp edge padding adhering to Material 3 8dp spacing grid.
  - Strict Edge-to-Edge with `.navigationBarsPadding()` and `.statusBarsPadding()` to eliminate system bar clipping.
  - Minimum touch target size of 48dp on all interactive elements.

---

## 3. Key Product Decisions & Trade-Offs

- **Exact Integer Arithmetic**:
  - All financial amounts are stored as 64-bit integer minor units (`paisa: Long`, where `100 paisa = 1 PKR`).
  - Money parser strictly validates user inputs, rejecting invalid fractional values.
- **Immutable Balance Calculation / Reactive Flow**:
  - Account balances are dynamically derived via SQL queries (`openingBalance + SUM(income) - SUM(expense) + SUM(transfers_in) - SUM(transfers_out)`), or maintained via atomic Room transaction triggers.
  - The UI observes Room `Flow`, updating dashboard, budgets, and accounts immediately upon database mutations.
- **No Automatic Payment Booking**:
  - Scheduled salary or recurring utility bills NEVER insert a financial transaction automatically. They create pending occurrences requiring user confirmation.
- **Isolated App-Private Receipts**:
  - Images selected through the photo picker are copied directly into `context.filesDir/receipts/` with unique IDs, avoiding permission issues and gallery deletion hazards.
- **Local JSON Backup & Safe Restore**:
  - Complete database state (accounts, categories, transactions, transfers, budgets, recurring rules) is serialized into a validated JSON backup with cryptographic hash and schema version.

---

## 4. Technical Architecture & Data Strategy

### System Component Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│  ┌───────────────┐ ┌────────────────┐ ┌──────────────┐ ┌─────────────┐ │
│  │   Dashboard   │ │  Transactions  │ │   Accounts   │ │   Budgets   │ │
│  │   & Summary   │ │  & Add Modal   │ │ & Transfers  │ │  & Reports  │ │
│  └───────▲───────┘ └───────▲────────┘ └──────▲───────┘ └──────▲──────┘ │
│  ┌───────┴───────┐ ┌───────┴────────┐ ┌──────┴───────┐ ┌──────┴──────┐ │
│  │  Reconcile UI │ │ Recurring Bills│ │Receipt Viewer│ │Backup/Diag  │ │
│  └───────▲───────┘ └───────▲────────┘ └──────▲───────┘ └──────▲──────┘ │
└──────────┼─────────────────┼─────────────────┼────────────────┼────────┘
           │                 │                 │                │
┌──────────┴─────────────────┴─────────────────┴────────────────┴────────┐
│                      HisaabViewModel & State Flows                     │
│  - HisaabUiState (NetBalance, AccountList, FilteredTxns, Budgets, etc.)│
│  - Financial Engine: formatPkr(), parsePaisa(), duplicateHeuristic()   │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ Coroutines & Flow
┌───────────────────────────────────┴────────────────────────────────────┐
│                           Repository Layer                             │
│  ┌──────────────────────┐ ┌──────────────────────┐ ┌─────────────────┐ │
│  │ TransactionRepo      │ │ AccountRepo          │ │ BudgetRepo      │ │
│  ├──────────────────────┤ ├──────────────────────┤ ├─────────────────┤ │
│  │ RecurringRepo        │ │ ReceiptStorageManager│ │ BackupManager   │ │
│  └──────────────────────┘ └──────────────────────┘ └─────────────────┘ │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │
┌───────────────────────────────────┴────────────────────────────────────┐
│                    Room SQLite Database (Version 1)                    │
│  ┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐  │
│  │ AccountEntity      │ │ CategoryEntity     │ │ TransactionEntity  │  │
│  ├────────────────────┤ ├────────────────────┤ ├────────────────────┤  │
│  │ TransferEntity     │ │ BudgetEntity       │ │ RecurringRuleEntity│  │
│  ├────────────────────┤ ├────────────────────┤ ├────────────────────┤  │
│  │ RecurringOccurEntity│ │ ReceiptEntity      │ │ ReconcileEntity    │  │
│  └────────────────────┘ └────────────────────┘ └────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Room Database Schema

1. **AccountEntity**:
   - `id: Long` (PK autogenerate)
   - `name: String` ("Cash in Hand", "Bank Account", "Mobile Wallet")
   - `type: String` ("CASH", "BANK", "WALLET", "SAVINGS")
   - `openingBalancePaisa: Long`
   - `colorHex: String`
   - `isArchived: Boolean`
   - `createdAt: Long`
2. **CategoryEntity**:
   - `id: Long` (PK autogenerate)
   - `name: String` ("Food & Dining", "Travel & Transport", "Shopping", "Bills & Utilities", "Salary", etc.)
   - `type: String` ("EXPENSE", "INCOME")
   - `iconName: String`
   - `colorHex: String`
   - `isCustom: Boolean`
3. **TransactionEntity**:
   - `id: Long` (PK autogenerate)
   - `type: String` ("EXPENSE", "INCOME", "ADJUSTMENT")
   - `amountPaisa: Long`
   - `accountId: Long` (FK -> accounts.id)
   - `categoryId: Long` (FK -> categories.id)
   - `dateEpochMs: Long`
   - `description: String`
   - `merchant: String`
   - `receiptRelativePath: String?`
   - `recurringOccurrenceId: Long?`
   - `createdAt: Long`
   - `updatedAt: Long`
4. **TransferEntity**:
   - `id: Long` (PK autogenerate)
   - `fromAccountId: Long` (FK -> accounts.id)
   - `toAccountId: Long` (FK -> accounts.id)
   - `amountPaisa: Long`
   - `dateEpochMs: Long`
   - `note: String`
   - `createdAt: Long`
5. **BudgetEntity**:
   - `id: Long` (PK autogenerate)
   - `categoryId: Long?` (null for overall monthly budget)
   - `monthKey: String` (format: "YYYY-MM")
   - `limitPaisa: Long`
6. **RecurringRuleEntity**:
   - `id: Long` (PK autogenerate)
   - `title: String`
   - `amountPaisa: Long`
   - `type: String` ("EXPENSE", "INCOME")
   - `accountId: Long`
   - `categoryId: Long`
   - `frequency: String` ("DAILY", "WEEKLY", "MONTHLY", "YEARLY")
   - `nextDueDateEpochMs: Long`
   - `isActive: Boolean`
7. **RecurringOccurrenceEntity**:
   - `id: Long` (PK autogenerate)
   - `ruleId: Long` (FK -> recurring_rules.id)
   - `dueDateEpochMs: Long`
   - `status: String` ("PENDING", "CONFIRMED", "SKIPPED", "POSTPONED")
   - `transactionId: Long?`
8. **ReconciliationEntity**:
   - `id: Long` (PK autogenerate)
   - `accountId: Long` (FK -> accounts.id)
   - `calculatedBalancePaisa: Long`
   - `statementBalancePaisa: Long`
   - `discrepancyPaisa: Long`
   - `dateEpochMs: Long`
   - `note: String`

---

## 5. Execution & Verification Checklist

Upon user approval of this plan:
1. **Initialize App Identity**: Update `metadata.json` and `res/values/strings.xml` to `"Hisaab do Farmanullah Ansari"`.
2. **Build Data Engine**:
   - Value object `Money` with exact `paisa: Long` formatting, parsing, and arithmetic.
   - Room Entities, TypeConverters, DAOs, and `HisaabDatabase`.
   - Seed starter accounts (**Cash in Hand**, **Bank Account**, **Mobile Wallet**) and default categories on first database creation.
3. **Build Repositories & Storage**:
   - `TransactionRepository` (atomic inserts, transfers, balance reconciliation, undo support).
   - `ReceiptStorageManager` (copy to internal files, thumbnail generation, cleanup).
   - `BackupRestoreManager` (export/import validated JSON archives).
4. **Compose UI Implementation**:
   - Central theme tokens in `Theme.kt` with custom Coral-to-Yellow gradient hero styles.
   - `DashboardScreen`: Gradient hero balance, monthly totals, upcoming alerts, starter account pills, recent transactions.
   - `TransactionsScreen`: Real search, filter chips, duplicate warning badge, receipt indicators.
   - `AddTransactionSheet`: Numeric keypad focus, fast toggles, dynamic category chips, receipt picker.
   - `AccountsScreen`: Individual balances, atomic transfer bottom sheet, bank statement reconciliation dialog.
   - `BudgetsScreen`: Monthly limits, visual progress meters, threshold warnings.
   - `RecurringScreen`: Pending bill occurrences, confirm & book action, skip/postpone actions.
   - `SettingsBackupScreen`: Local JSON export, verified restore with preview, data integrity diagnostics.
5. **Compilation Verification**: Run `compile_applet` to verify clean build without warnings or compilation failures.
