# Problem Statement and Project Scope

# 1. Introduction
Manual record keeping of book issues, returns and overdue tracking is difficult for college and departmental libraries. Paper registers and disorganised spreadsheets lead to misplaced records, delayed returns and errors in calculating overdue penalties. 

The objective of this project is to develop a smart library management system in console based pure java (standalone JDK) which is lightweight and automates book cataloguing, member account management, book borrow/return with fine calculation, background overdue reminder and activity logging.

## 2. Who are the target users?
- **Librarians / System Administrators**: These guys maintain the book catalogue (add, update, delete books), view member lists, inspect full transaction histories and generate text reports.
- **Library Members (Students / Faculty)**: Search catalogue by title/author/ISBN; Borrow available books; Return books; View their active borrowing limits.

## 3. Scope of the project
The system includes the following functional modules:
1. **User Authentication & Role Management**: Different roles for Admin & Member with polymorphic profile behaviour.
2. **Book Catalogue Management**: Full CRUD operations for books stored in text files, and cached in memory.
3. **Borrow / Return & Fine Engine**: Check for borrowing limits (max 3 books per member). Verify that the book is available for borrowing. Calculate fines (Rs. 5 per day) if the book is returned late.
4. **Background Overdue Reminder Service**: a background thread that periodically checks active records and logs overdue reminders.
5. **Reports & Activity Logging**: Support for on-demand generation of formatted transaction history text files and persistent buffered action logging.

## 4. Course Syllabus Alignment
The application directly demonstrates concepts across the core Java syllabus:
- **Unit 1 (Java Basics & Flow Control)**: Loops (`while`, `for-each`), conditional statements (`if-else`), `switch-case`, `Scanner` input, `System.out` formatting.
- **Unit 2 (OOP Fundamentals)**: Abstract class (`User`), Inheritance (`Admin`, `Member`), Interface (`Borrowable`), Polymorphism (overriding `displayRoleDetails` and `canBorrow`), Encapsulation, Enum (`BookStatus`).
- **Unit 3 (Exception Handling & Multithreading)**: Custom exception hierarchy (`LibraryException`, `BookNotAvailableException`, `MemberLimitExceededException`, `InvalidOperationException`), `try-catch` blocks, background daemon thread (`ReminderThread`), and `synchronized` blocks.
- **Unit 4 (Collections & File I/O Streams)**: Collections framework (`ArrayList`, `HashMap`), character streams (`BufferedReader`, `BufferedWriter`, `FileReader`, `FileWriter`) for full persistent data storage (`books.txt`, `members.txt`, `transactions.txt`), activity logging (`library.log`), and report exporting (`transaction_report.txt`).
