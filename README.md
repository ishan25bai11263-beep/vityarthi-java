# Intelligent Library Management System

Library Management System developed in pure Java (standard JDK only, zero external libraries) for a college course assignment, "Programming in Java". Console based.

## Summary
The system provides a menu driven command line interface to manage library books, user accounts and transactions. It provides role based access for Administrators and Members, an automated fine calculation engine for late returns, in-memory caching over persistent file based storage and a background reminder daemon thread for overdue loan detection.


## Feature
- **Authentication**: Login for Admin and Members are different.
- **Books Management (CRUD)**: Add, update, delete, view and search books.
- **Borrow & Return Engine**- Verify the availability of the book before issuing.
  - Each member can borrow a maximum of 3 books at a time.
  - Computes late fine of Rs. 5 per day from the due date on return
- **Overdue Reminder Service** Background daemon thread that scans active transactions and writes overdue alerts to log files.
- **Activity Logging & Report Generation**: Records user activities to `data/library.log` and creates formatted transaction reports to `data/transaction_report.txt`.- **In-Memory Cache & File Persistence:** Uses `ArrayList<Book>` and `HashMap<Integer, Member>` to enable fast lookups in memory, while file storage (`data/books.txt`, `data/members.txt`, `data/transactions.txt`) with `BufferedReader` and `BufferedWriter` ensures persistence.

## Tech Stack & Requirements
- **Language**: Pure Java (Standard JDK 8 or higher)
- **External Dependencies**: None (0 `.jar` files required)
- **Storage**: Standard Java File I/O (`java.io.BufferedReader`, `java.io.BufferedWriter`, `java.io.FileReader`, `java.io.FileWriter`)

## Setup and Running

### 1. Compile the Project
From the project root directory, run:
```bash
javac -d bin src/*.java
```

### 2. Run the Application
```bash
java -cp bin Main
```

### 3. Default Login Credentials
- **Admin**:
  - Email: `admin@library.com`
  - Password: `admin123`
- **Sample Member**:
  - Email: `rahul@student.edu`
  - Password: `pass123`
- Or register a new member account from the main menu.
