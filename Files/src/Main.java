import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}

class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String message) {
        super(message);
    }
}

class MemberLimitExceededException extends LibraryException {
    public MemberLimitExceededException(String message) {
        super(message);
    }
}

class InvalidOperationException extends LibraryException {
    public InvalidOperationException(String message) {
        super(message);
    }
}

enum BookStatus {
    AVAILABLE,
    BORROWED
}

interface Borrowable {
    boolean isAvailable();
    void borrowItem();
    void returnItem();
}

class Book implements Borrowable {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private BookStatus status;

    public Book(int id, String title, String author, String isbn, BookStatus status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.status = status;
    }

    public Book(String title, String author, String isbn) {
        this(0, title, author, isbn, BookStatus.AVAILABLE);
    }

    @Override
    public boolean isAvailable() {
        return this.status == BookStatus.AVAILABLE;
    }

    @Override
    public void borrowItem() {
        this.status = BookStatus.BORROWED;
    }

    @Override
    public void returnItem() {
        this.status = BookStatus.AVAILABLE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%d] \"%s\" by %s (ISBN: %s) - %s", id, title, author, isbn, status);
    }
}

abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected String role;

    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public abstract void displayRoleDetails();
    public abstract boolean canBorrow();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return id + ": " + name + " (" + email + ") [" + role + "]";
    }
}

class Admin extends User {
    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, "ADMIN");
    }

    @Override
    public void displayRoleDetails() {
        System.out.println("Role          : Administrator");
        System.out.println("Privileges    : Full System Control (Catalog CRUD, Analytics, Audit Logs, Members)");
    }

    @Override
    public boolean canBorrow() {
        return false;
    }
}

class Member extends User {
    public static final int MAX_BORROW_LIMIT = 3;
    public static final double MAX_ALLOWED_FINE_FOR_BORROW = 50.0;
    private int borrowedCount;

    public Member(int id, String name, String email, String password, int borrowedCount) {
        super(id, name, email, password, "MEMBER");
        this.borrowedCount = borrowedCount;
    }

    public Member(int id, String name, String email, String password) {
        this(id, name, email, password, 0);
    }

    @Override
    public void displayRoleDetails() {
        System.out.println("Role          : Library Member");
        System.out.println("Borrowed Books: " + borrowedCount + "/" + MAX_BORROW_LIMIT + " active loans");
        System.out.println("Fine Policy   : Borrowing paused if unpaid fines exceed Rs. " + MAX_ALLOWED_FINE_FOR_BORROW);
    }

    @Override
    public boolean canBorrow() {
        return this.borrowedCount < MAX_BORROW_LIMIT;
    }

    public int getBorrowedCount() { return borrowedCount; }
    public void setBorrowedCount(int borrowedCount) { this.borrowedCount = borrowedCount; }
    public void incrementBorrowedCount() { this.borrowedCount++; }
    public void decrementBorrowedCount() {
        if (this.borrowedCount > 0) this.borrowedCount--;
    }
}

class Transaction {
    private int id;
    private int bookId;
    private int memberId;
    private String issueDate;
    private String dueDate;
    private String returnDate;
    private double fine;

    public static final double DAILY_FINE_RATE = 5.00;
    public static final double MAX_FINE_CAP = 250.00;

    public Transaction(int id, int bookId, int memberId, String issueDate, String dueDate, String returnDate, double fine) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
    }

    public Transaction(int bookId, int memberId, String issueDate, String dueDate) {
        this(0, bookId, memberId, issueDate, dueDate, null, 0.0);
    }

    public double calculateFine(String actualReturnDate) {
        try {
            LocalDate due = LocalDate.parse(this.dueDate);
            LocalDate ret = LocalDate.parse(actualReturnDate);
            if (ret.isAfter(due)) {
                long daysLate = ChronoUnit.DAYS.between(due, ret);
                double computed = daysLate * DAILY_FINE_RATE;
                this.fine = Math.min(computed, MAX_FINE_CAP);
            } else {
                this.fine = 0.0;
            }
        } catch (Exception e) {
            this.fine = 0.0;
        }
        return this.fine;
    }

    public boolean isOverdue(String currentDate) {
        if (returnDate != null) return false;
        try {
            LocalDate due = LocalDate.parse(dueDate);
            LocalDate curr = LocalDate.parse(currentDate);
            return curr.isAfter(due);
        } catch (Exception e) {
            return false;
        }
    }

    public long getDaysOverdue(String currentDate) {
        if (!isOverdue(currentDate)) return 0;
        try {
            LocalDate due = LocalDate.parse(dueDate);
            LocalDate curr = LocalDate.parse(currentDate);
            return ChronoUnit.DAYS.between(due, curr);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }

    @Override
    public String toString() {
        return String.format("Tx #%-3d | Book ID: %-3d | Member ID: %-3d | Issued: %s | Due: %s | %s",
                id, bookId, memberId, issueDate, dueDate,
                (returnDate != null ? "Returned: " + returnDate + " (Fine: Rs. " + String.format("%.2f", fine) + ")" : "[ACTIVE LOAN]"));
    }
}

class LibraryService {
    private final LibraryStorage storage;
    private final List<Book> bookCache;
    private final Map<Integer, Member> memberCache;

    public LibraryService() {
        this.storage = new LibraryStorage();
        this.bookCache = new ArrayList<>();
        this.memberCache = new HashMap<>();
        refreshCache();
    }

    public synchronized void refreshCache() {
        bookCache.clear();
        bookCache.addAll(storage.getAllBooks());

        memberCache.clear();
        for (Member m : storage.getAllMembers()) {
            memberCache.put(m.getId(), m);
        }
    }

    public synchronized boolean addBook(Book book) {
        boolean ok = storage.addBook(book);
        if (ok) {
            bookCache.add(book);
            FileUtil.log("ADD_BOOK", "Added book: \"" + book.getTitle() + "\" (ID: " + book.getId() + ", ISBN: " + book.getIsbn() + ")");
        }
        return ok;
    }

    public synchronized boolean updateBook(Book book) {
        boolean ok = storage.updateBook(book);
        if (ok) {
            refreshCache();
            FileUtil.log("UPDATE_BOOK", "Updated book ID: " + book.getId());
        }
        return ok;
    }

    public synchronized boolean deleteBook(int bookId) {
        Book b = getBookById(bookId);
        if (b != null && !b.isAvailable()) {
            return false;
        }

        boolean ok = storage.deleteBook(bookId);
        if (ok) {
            bookCache.removeIf(item -> item.getId() == bookId);
            FileUtil.log("DELETE_BOOK", "Deleted book ID: " + bookId);
        }
        return ok;
    }

    public synchronized List<Book> getAllBooks() {
        return new ArrayList<>(bookCache);
    }

    public synchronized Book getBookById(int id) {
        for (Book b : bookCache) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    public synchronized List<Book> searchBooks(String query, String statusFilter) {
        List<Book> results = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase();

        for (Book b : bookCache) {
            boolean matchesQuery = q.isEmpty() ||
                    b.getTitle().toLowerCase().contains(q) ||
                    b.getAuthor().toLowerCase().contains(q) ||
                    b.getIsbn().toLowerCase().contains(q);

            boolean matchesStatus = true;
            if ("AVAILABLE".equalsIgnoreCase(statusFilter)) {
                matchesStatus = b.isAvailable();
            } else if ("BORROWED".equalsIgnoreCase(statusFilter)) {
                matchesStatus = !b.isAvailable();
            }

            if (matchesQuery && matchesStatus) {
                results.add(b);
            }
        }
        return results;
    }

    public synchronized List<Book> getSortedBooks(int sortOption) {
        List<Book> list = new ArrayList<>(bookCache);
        switch (sortOption) {
            case 1:
                list.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case 2:
                list.sort(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
                break;
            case 3:
                list.sort(Comparator.comparingInt(Book::getId));
                break;
            case 4:
                Map<Integer, Integer> borrowCounts = getBorrowCountsMap();
                list.sort((b1, b2) -> Integer.compare(
                        borrowCounts.getOrDefault(b2.getId(), 0),
                        borrowCounts.getOrDefault(b1.getId(), 0)
                ));
                break;
            default:
                break;
        }
        return list;
    }

    public synchronized Map<Integer, Integer> getBorrowCountsMap() {
        Map<Integer, Integer> map = new HashMap<>();
        for (Transaction tx : storage.getAllTransactions()) {
            map.put(tx.getBookId(), map.getOrDefault(tx.getBookId(), 0) + 1);
        }
        return map;
    }

    public User login(String email, String password) {
        User user = storage.authenticate(email, password);
        if (user != null) {
            FileUtil.log("LOGIN", "User logged in: " + user.getEmail() + " (" + user.getRole() + ")");
        }
        return user;
    }

    public synchronized boolean registerMember(Member member) {
        boolean ok = storage.addMember(member);
        if (ok) {
            memberCache.put(member.getId(), member);
            FileUtil.log("REGISTER", "Registered member: " + member.getName() + " (ID: " + member.getId() + ")");
        }
        return ok;
    }

    public synchronized Member getMember(int id) {
        Member m = memberCache.get(id);
        if (m == null) {
            m = storage.getMemberById(id);
            if (m != null) memberCache.put(m.getId(), m);
        }
        return m;
    }

    public synchronized List<Member> getAllMembers() {
        return storage.getAllMembers();
    }

    public synchronized double getMemberOutstandingFines(int memberId) {
        return storage.getTotalFinesByMember(memberId);
    }

    public synchronized boolean hasOverdueBooks(int memberId) {
        String today = LocalDate.now().toString();
        for (Transaction tx : storage.getTransactionsByMember(memberId)) {
            if (tx.getReturnDate() == null && tx.isOverdue(today)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Transaction issueBook(int bookId, int memberId) throws LibraryException {
        Book book = getBookById(bookId);
        if (book == null) {
            throw new InvalidOperationException("Book with ID #" + bookId + " does not exist in catalog.");
        }

        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book \"" + book.getTitle() + "\" is already checked out by another member.");
        }

        Member member = getMember(memberId);
        if (member == null) {
            throw new InvalidOperationException("Member ID #" + memberId + " not found.");
        }

        if (!member.canBorrow()) {
            throw new MemberLimitExceededException("Borrow limit reached! Maximum " + Member.MAX_BORROW_LIMIT + " books allowed concurrently.");
        }

        double unpaidFines = getMemberOutstandingFines(memberId);
        if (unpaidFines > Member.MAX_ALLOWED_FINE_FOR_BORROW) {
            throw new InvalidOperationException(String.format("Borrowing blocked! You have accumulated Rs. %.2f in unpaid fines (Limit: Rs. %.2f).",
                    unpaidFines, Member.MAX_ALLOWED_FINE_FOR_BORROW));
        }

        if (hasOverdueBooks(memberId)) {
            throw new InvalidOperationException("Borrowing blocked! You currently have overdue books. Please return them first.");
        }

        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(14);

        Transaction tx = new Transaction(bookId, memberId, today.toString(), due.toString());
        boolean saved = storage.addTransaction(tx);
        if (!saved) {
            throw new LibraryException("Storage error: Failed to record borrow transaction.");
        }

        book.borrowItem();
        storage.updateBook(book);

        member.incrementBorrowedCount();
        storage.updateBorrowedCount(member.getId(), member.getBorrowedCount());

        FileUtil.log("ISSUE_BOOK", "Book ID #" + bookId + " (\"" + book.getTitle() + "\") issued to Member ID #" + memberId + " (Due: " + due + ")");
        return tx;
    }

    public synchronized double returnBook(int bookId, int memberId) throws LibraryException {
        Transaction tx = storage.getActiveTransaction(bookId, memberId);
        if (tx == null) {
            throw new InvalidOperationException("No active borrow record found for Book ID #" + bookId + " and Member ID #" + memberId + ".");
        }

        String today = LocalDate.now().toString();
        double fine = tx.calculateFine(today);

        boolean updated = storage.updateReturn(tx.getId(), today, fine);
        if (!updated) {
            throw new LibraryException("Storage error: Failed to finalize book return.");
        }

        Book book = getBookById(bookId);
        if (book != null) {
            book.returnItem();
            storage.updateBook(book);
        }

        Member member = getMember(memberId);
        if (member != null) {
            member.decrementBorrowedCount();
            storage.updateBorrowedCount(member.getId(), member.getBorrowedCount());
        }

        FileUtil.log("RETURN_BOOK", "Book ID #" + bookId + " returned by Member ID #" + memberId + ". Fine assessed: Rs. " + String.format("%.2f", fine));
        return fine;
    }

    public synchronized List<Transaction> getActiveTransactions() {
        return storage.getActiveTransactions();
    }

    public synchronized List<Transaction> getAllTransactions() {
        return storage.getAllTransactions();
    }

    public synchronized List<Transaction> getMemberTransactions(int memberId) {
        return storage.getTransactionsByMember(memberId);
    }
}

class ReminderThread extends Thread {
    private final LibraryService libraryService;
    private volatile boolean running = true;
    private final long checkIntervalMs;
    private final List<String> overdueAlerts = Collections.synchronizedList(new ArrayList<>());

    public ReminderThread(LibraryService libraryService, long checkIntervalMs) {
        super("Reminder-Daemon");
        this.libraryService = libraryService;
        this.checkIntervalMs = checkIntervalMs;
        setDaemon(true);
    }

    public ReminderThread(LibraryService libraryService) {
        this(libraryService, 25000);
    }

    public void stopReminderService() {
        this.running = false;
        this.interrupt();
    }

    public List<String> getOverdueAlertsSnapshot() {
        synchronized (overdueAlerts) {
            return new ArrayList<>(overdueAlerts);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                scanOverdueTransactions();
                Thread.sleep(checkIntervalMs);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("[ReminderDaemon Warning] " + e.getMessage());
            }
        }
    }

    private void scanOverdueTransactions() {
        String today = LocalDate.now().toString();
        List<Transaction> activeList;

        synchronized (libraryService) {
            activeList = libraryService.getActiveTransactions();
        }

        List<String> currentScanAlerts = new ArrayList<>();
        for (Transaction tx : activeList) {
            if (tx.isOverdue(today)) {
                long days = tx.getDaysOverdue(today);
                String alert = String.format("ALERT: Tx #%d | Book ID #%d | Member ID #%d | Due: %s (%d days overdue)",
                        tx.getId(), tx.getBookId(), tx.getMemberId(), tx.getDueDate(), days);
                currentScanAlerts.add(alert);
                FileUtil.log("OVERDUE_SCANNER", alert);
            }
        }

        synchronized (overdueAlerts) {
            overdueAlerts.clear();
            overdueAlerts.addAll(currentScanAlerts);
        }
    }
}

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static LibraryService libraryService;
    private static ReminderThread reminderThread;

    public static void main(String[] args) {
        libraryService = new LibraryService();
        
        reminderThread = new ReminderThread(libraryService);
        reminderThread.start();

        printHeader("SMART LIBRARY MANAGEMENT SYSTEM (v2.0)");

        boolean running = true;
        while (running) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║               MAIN MENU                ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║  1. System Login (Admin / Member)      ║");
            System.out.println("║  2. Register as New Member             ║");
            System.out.println("║  3. Browse & Search Book Catalog       ║");
            System.out.println("║  4. Exit System                        ║");
            System.out.println("╚════════════════════════════════════════╝");

            int choice = readIntPrompt("Select option (1-4): ", 1, 4);
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegistration();
                    break;
                case 3:
                    handleCatalogBrowsing();
                    break;
                case 4:
                    System.out.println("\n[SYSTEM] Terminating daemon services...");
                    reminderThread.stopReminderService();
                    System.out.println("[SYSTEM] Thank you for using Smart Library Management System. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    private static int readIntPrompt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please enter a number between " + min + " and " + max + ".");
                continue;
            }
            try {
                int val = Integer.parseInt(input);
                if (val < min || val > max) {
                    System.out.println("Out of range! Please enter a value between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input! Please enter an integer.");
            }
        }
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Field cannot be empty. Please enter a valid value.");
        }
    }

    private static String readValidEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (EMAIL_PATTERN.matcher(input).matches()) {
                return input;
            }
            System.out.println("Invalid email format (e.g. name@domain.com)! Please try again.");
        }
    }

    private static void printHeader(String title) {
        System.out.println("\n==========================================================================");
        System.out.println("                      " + title);
        System.out.println("==========================================================================");
    }

    private static void handleLogin() {
        printHeader("USER AUTHENTICATION");
        String email = readValidEmail("Enter Email: ");
        String password = readNonEmptyString("Enter Password: ");

        User user = libraryService.login(email, password);
        if (user == null) {
            System.out.println("\n[ERROR] Authentication failed! Incorrect email or password.");
            return;
        }

        System.out.println("\n>>> Login successful! Welcome, " + user.getName() + " <<<");
        user.displayRoleDetails();

        if (user instanceof Admin) {
            adminMenu((Admin) user);
        } else if (user instanceof Member) {
            memberMenu((Member) user);
        }
    }

    private static void handleRegistration() {
        printHeader("NEW MEMBER REGISTRATION");
        String name = readNonEmptyString("Enter Full Name: ");
        String email = readValidEmail("Enter Email Address: ");
        String password = readNonEmptyString("Enter Account Password: ");

        Member newMember = new Member(0, name, email, password);
        boolean success = libraryService.registerMember(newMember);
        if (success) {
            System.out.println("\n[SUCCESS] Registration complete! Your Member ID is: #" + newMember.getId());
            System.out.println("[INFO] You can now log in using your email: " + email);
        } else {
            System.out.println("\n[ERROR] Registration failed. The email '" + email + "' is already registered.");
        }
    }

    private static void handleCatalogBrowsing() {
        boolean inCatalog = true;
        while (inCatalog) {
            System.out.println("\n--- CATALOG EXPLORER ---");
            System.out.println("1. View All Books (Default Order)");
            System.out.println("2. Sort Catalog (Title / Author / Popularity)");
            System.out.println("3. Search by Keyword (Title / Author / ISBN)");
            System.out.println("4. Filter by Status (Available vs Borrowed)");
            System.out.println("5. Return to Main Menu");

            int ch = readIntPrompt("Select option (1-5): ", 1, 5);
            switch (ch) {
                case 1:
                    renderCatalogTable(libraryService.getAllBooks(), "COMPLETE BOOK CATALOG");
                    break;
                case 2:
                    System.out.println("\nSort options: 1) Title A-Z | 2) Author A-Z | 3) Book ID | 4) Most Popular");
                    int sortCh = readIntPrompt("Select sort criteria (1-4): ", 1, 4);
                    renderCatalogTable(libraryService.getSortedBooks(sortCh), "SORTED CATALOG VIEW");
                    break;
                case 3:
                    String kw = readNonEmptyString("Enter search keyword: ");
                    List<Book> results = libraryService.searchBooks(kw, "ALL");
                    renderCatalogTable(results, "SEARCH RESULTS FOR: \"" + kw + "\"");
                    break;
                case 4:
                    System.out.println("Filter: 1) Available Books Only | 2) Currently Borrowed Books Only");
                    int filterCh = readIntPrompt("Select filter (1-2): ", 1, 2);
                    String filterStr = filterCh == 1 ? "AVAILABLE" : "BORROWED";
                    renderCatalogTable(libraryService.searchBooks("", filterStr), filterStr + " BOOKS");
                    break;
                case 5:
                    inCatalog = false;
                    break;
            }
        }
    }

    private static void renderCatalogTable(List<Book> books, String tableTitle) {
        System.out.println("\n==========================================================================================");
        System.out.println(" " + tableTitle + " (" + books.size() + " records found)");
        System.out.println("==========================================================================================");
        if (books.isEmpty()) {
            System.out.println("  No books found matching the selected criteria.");
            System.out.println("==========================================================================================");
            return;
        }

        System.out.printf("%-5s | %-32s | %-22s | %-16s | %-10s%n", "ID", "TITLE", "AUTHOR", "ISBN", "STATUS");
        System.out.println("------------------------------------------------------------------------------------------");
        for (Book b : books) {
            String titleTrunc = b.getTitle().length() > 30 ? b.getTitle().substring(0, 27) + "..." : b.getTitle();
            String authTrunc = b.getAuthor().length() > 20 ? b.getAuthor().substring(0, 18) + "..." : b.getAuthor();
            System.out.printf("%-5d | %-32s | %-22s | %-16s | %-10s%n",
                    b.getId(), titleTrunc, authTrunc, b.getIsbn(), b.getStatus());
        }
        System.out.println("==========================================================================================");
    }

    private static void adminMenu(Admin admin) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n╔════════════════════════════════════════════════════╗");
            System.out.println("║                 ADMIN DASHBOARD                    ║");
            System.out.println("╠════════════════════════════════════════════════════╣");
            System.out.println("║  1. Add New Book to Catalog                        ║");
            System.out.println("║  2. Update Existing Book Details                   ║");
            System.out.println("║  3. Delete Book from Catalog                       ║");
            System.out.println("║  4. Browse & Sort Book Catalog                     ║");
            System.out.println("║  5. View All Registered Members & Loan History     ║");
            System.out.println("║  6. View Complete Transaction Ledger               ║");
            System.out.println("║  7. View Live Overdue Alerts (Background Monitor)  ║");
            System.out.println("║  8. View In-Console Analytics & Summary            ║");
            System.out.println("║  9. Export Audit & Transaction Report (.txt)       ║");
            System.out.println("║  0. Logout Admin Session                           ║");
            System.out.println("╚════════════════════════════════════════════════════╝");

            int ch = readIntPrompt("Select admin action (0-9): ", 0, 9);
            switch (ch) {
                case 1:
                    printHeader("ADD NEW BOOK");
                    String title = readNonEmptyString("Enter Book Title: ");
                    String author = readNonEmptyString("Enter Author Name: ");
                    String isbn = readNonEmptyString("Enter ISBN: ");
                    Book b = new Book(title, author, isbn);
                    if (libraryService.addBook(b)) {
                        System.out.println("\n[SUCCESS] Book added successfully! Assigned Book ID: #" + b.getId());
                    } else {
                        System.out.println("\n[ERROR] Failed to add book. A book with ISBN '" + isbn + "' already exists.");
                    }
                    break;
                case 2:
                    printHeader("UPDATE BOOK DETAILS");
                    int bId = readIntPrompt("Enter Book ID to update: ", 1, 999999);
                    Book existing = libraryService.getBookById(bId);
                    if (existing == null) {
                        System.out.println("[ERROR] Book with ID #" + bId + " not found!");
                        break;
                    }
                    System.out.println("Updating Book: \"" + existing.getTitle() + "\" (Leave field empty to keep current value)");
                    System.out.print("New Title [" + existing.getTitle() + "]: ");
                    String nt = scanner.nextLine().trim();
                    System.out.print("New Author [" + existing.getAuthor() + "]: ");
                    String na = scanner.nextLine().trim();
                    System.out.print("New ISBN [" + existing.getIsbn() + "]: ");
                    String ni = scanner.nextLine().trim();

                    if (!nt.isEmpty()) existing.setTitle(nt);
                    if (!na.isEmpty()) existing.setAuthor(na);
                    if (!ni.isEmpty()) existing.setIsbn(ni);

                    if (libraryService.updateBook(existing)) {
                        System.out.println("\n[SUCCESS] Book ID #" + bId + " updated successfully!");
                    } else {
                        System.out.println("\n[ERROR] Update failed! ISBN may conflict with another existing book.");
                    }
                    break;
                case 3:
                    printHeader("DELETE BOOK");
                    int delId = readIntPrompt("Enter Book ID to delete: ", 1, 999999);
                    Book bookToDelete = libraryService.getBookById(delId);
                    if (bookToDelete == null) {
                        System.out.println("[ERROR] Book ID #" + delId + " not found.");
                        break;
                    }
                    if (!bookToDelete.isAvailable()) {
                        System.out.println("[ERROR] Cannot delete Book ID #" + delId + " because it is currently BORROWED.");
                        break;
                    }
                    if (libraryService.deleteBook(delId)) {
                        System.out.println("\n[SUCCESS] Book ID #" + delId + " (\"" + bookToDelete.getTitle() + "\") was deleted.");
                    } else {
                        System.out.println("\n[ERROR] Could not delete book.");
                    }
                    break;
                case 4:
                    handleCatalogBrowsing();
                    break;
                case 5:
                    displayMembersAndHistory();
                    break;
                case 6:
                    displayAllTransactionsTable();
                    break;
                case 7:
                    displayLiveOverdueAlerts();
                    break;
                case 8:
                    displaySystemAnalytics();
                    break;
                case 9:
                    exportReportFile();
                    break;
                case 0:
                    inMenu = false;
                    System.out.println("\n[INFO] Admin session logged out successfully.");
                    break;
            }
        }
    }

    private static void displayMembersAndHistory() {
        printHeader("REGISTERED MEMBERS & LOAN HISTORY");
        List<Member> members = libraryService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No registered members found.");
            return;
        }

        System.out.printf("%-6s | %-24s | %-28s | %-14s | %-12s%n", "ID", "NAME", "EMAIL", "ACTIVE LOANS", "TOTAL FINES");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (Member m : members) {
            double fines = libraryService.getMemberOutstandingFines(m.getId());
            System.out.printf("%-6d | %-24s | %-28s | %-14d | Rs. %-10.2f%n",
                    m.getId(), m.getName(), m.getEmail(), m.getBorrowedCount(), fines);
        }
        System.out.println("==================================================================================================");

        System.out.print("\nEnter Member ID to inspect full loan history (or press Enter to skip): ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                int mId = Integer.parseInt(input);
                List<Transaction> mTxList = libraryService.getMemberTransactions(mId);
                System.out.println("\n--- TRANSACTION HISTORY FOR MEMBER #" + mId + " (" + mTxList.size() + " records) ---");
                if (mTxList.isEmpty()) {
                    System.out.println("No transaction records found for this member.");
                } else {
                    for (Transaction tx : mTxList) {
                        System.out.println(tx);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input!");
            }
        }
    }

    private static void displayAllTransactionsTable() {
        printHeader("COMPLETE TRANSACTION LEDGER");
        List<Transaction> txList = libraryService.getAllTransactions();
        if (txList.isEmpty()) {
            System.out.println("No transactions recorded in system yet.");
            return;
        }

        String today = LocalDate.now().toString();
        System.out.printf("%-5s | %-7s | %-9s | %-11s | %-11s | %-11s | %-10s | %-8s%n",
                "TX ID", "BOOK ID", "MEMBER ID", "ISSUE DATE", "DUE DATE", "RETURN DATE", "STATUS", "FINE");
        System.out.println("------------------------------------------------------------------------------------------------");
        for (Transaction tx : txList) {
            String retStr = tx.getReturnDate() == null ? "-" : tx.getReturnDate();
            String status = tx.getReturnDate() != null ? "RETURNED" : (tx.isOverdue(today) ? "OVERDUE" : "ACTIVE");
            System.out.printf("%-5d | %-7d | %-9d | %-11s | %-11s | %-11s | %-10s | Rs. %-6.2f%n",
                    tx.getId(), tx.getBookId(), tx.getMemberId(), tx.getIssueDate(), tx.getDueDate(), retStr, status, tx.getFine());
        }
        System.out.println("================================================================================================");
    }

    private static void displayLiveOverdueAlerts() {
        printHeader("BACKGROUND OVERDUE MONITOR");
        List<String> alerts = reminderThread.getOverdueAlertsSnapshot();
        if (alerts.isEmpty()) {
            System.out.println("[STATUS: ALL CLEAR] No overdue books detected during latest background scan.");
        } else {
            System.out.println("[STATUS: ACTION REQUIRED] " + alerts.size() + " overdue borrow record(s) flagged:\n");
            for (String alert : alerts) {
                System.out.println("  • " + alert);
            }
        }
    }

    private static void displaySystemAnalytics() {
        printHeader("SYSTEM ANALYTICS & EXECUTIVE SUMMARY");
        List<Book> books = libraryService.getAllBooks();
        List<Member> members = libraryService.getAllMembers();
        List<Transaction> transactions = libraryService.getAllTransactions();
        String today = LocalDate.now().toString();

        int activeCount = 0;
        int overdueCount = 0;
        double totalFines = 0.0;
        Map<Integer, Integer> borrowMap = libraryService.getBorrowCountsMap();

        for (Transaction tx : transactions) {
            totalFines += tx.getFine();
            if (tx.getReturnDate() == null) {
                activeCount++;
                if (tx.isOverdue(today)) {
                    overdueCount++;
                }
            }
        }

        int topBookId = -1;
        int maxBorrows = 0;
        for (Map.Entry<Integer, Integer> entry : borrowMap.entrySet()) {
            if (entry.getValue() > maxBorrows) {
                maxBorrows = entry.getValue();
                topBookId = entry.getKey();
            }
        }

        String topBookTitle = "None";
        if (topBookId != -1) {
            Book topB = libraryService.getBookById(topBookId);
            if (topB != null) {
                topBookTitle = "\"" + topB.getTitle() + "\" (" + maxBorrows + " borrows)";
            }
        }

        System.out.printf("  • Total Books Cataloged       : %d%n", books.size());
        System.out.printf("  • Total Registered Members     : %d%n", members.size());
        System.out.printf("  • Total Recorded Transactions  : %d%n", transactions.size());
        System.out.printf("  • Active Borrow Transactions   : %d%n", activeCount);
        System.out.printf("  • Currently Overdue Loans      : %d%n", overdueCount);
        System.out.printf("  • Total Fines Accrued          : Rs. %.2f%n", totalFines);
        System.out.printf("  • Most Popular Book            : %s%n", topBookTitle);
        System.out.println("==========================================================================");
    }

    private static void exportReportFile() {
        printHeader("EXPORT TRANSACTION AUDIT REPORT");
        String path = "data/transaction_report.txt";
        List<Transaction> txList = libraryService.getAllTransactions();
        List<Book> books = libraryService.getAllBooks();
        List<Member> members = libraryService.getAllMembers();

        if (FileUtil.exportDetailedReport(txList, books, members, path)) {
            System.out.println("\n[SUCCESS] Comprehensive audit report exported to: " + path);
            System.out.println("[INFO] Contains Executive Summary, Member Fine Ledger, and Full Transaction Log.");
        } else {
            System.out.println("\n[ERROR] Failed to write report file.");
        }
    }

    private static void memberMenu(Member member) {
        boolean inMenu = true;
        while (inMenu) {
            Member currentMember = libraryService.getMember(member.getId());
            if (currentMember != null) member = currentMember;

            System.out.println("\n╔════════════════════════════════════════════════════╗");
            System.out.println("║                 MEMBER DASHBOARD                   ║");
            System.out.println("╠════════════════════════════════════════════════════╣");
            System.out.println("║  1. Browse & Search Book Catalog                   ║");
            System.out.println("║  2. Borrow a Book                                  ║");
            System.out.println("║  3. Return a Borrowed Book                         ║");
            System.out.println("║  4. View My Profile & Borrow Status                ║");
            System.out.println("║  5. View My Borrow History & Past Fines            ║");
            System.out.println("║  0. Logout Member Session                          ║");
            System.out.println("╚════════════════════════════════════════════════════╝");

            int ch = readIntPrompt("Select member action (0-5): ", 0, 5);
            switch (ch) {
                case 1:
                    handleCatalogBrowsing();
                    break;
                case 2:
                    handleBorrowBook(member);
                    break;
                case 3:
                    handleReturnBook(member);
                    break;
                case 4:
                    printHeader("MY ACCOUNT PROFILE");
                    member.displayRoleDetails();
                    double unpaidFines = libraryService.getMemberOutstandingFines(member.getId());
                    System.out.printf("Total Accumulated Fines : Rs. %.2f%n", unpaidFines);
                    if (libraryService.hasOverdueBooks(member.getId())) {
                        System.out.println("[WARNING] You have overdue books! Please return them immediately to avoid extra penalties.");
                    }
                    break;
                case 5:
                    displayMemberHistory(member);
                    break;
                case 0:
                    inMenu = false;
                    System.out.println("\n[INFO] Member session logged out successfully.");
                    break;
            }
        }
    }

    private static void handleBorrowBook(Member member) {
        printHeader("BORROW A BOOK");
        int bId = readIntPrompt("Enter Book ID to borrow: ", 1, 999999);
        try {
            Transaction tx = libraryService.issueBook(bId, member.getId());
            Book b = libraryService.getBookById(bId);
            String title = b != null ? b.getTitle() : "Book #" + bId;
            System.out.println("\n[SUCCESS] \"" + title + "\" borrowed successfully!");
            System.out.println("Transaction ID : #" + tx.getId());
            System.out.println("Issue Date     : " + tx.getIssueDate());
            System.out.println("Due Date       : " + tx.getDueDate() + " (14-day borrowing window)");
            System.out.println("Please return by the due date to avoid late fees of Rs. 5.00/day.");
        } catch (LibraryException e) {
            System.out.println("\n[BORROW FAILED] " + e.getMessage());
        }
    }

    private static void handleReturnBook(Member member) {
        printHeader("RETURN A BORROWED BOOK");
        int bId = readIntPrompt("Enter Book ID to return: ", 1, 999999);
        try {
            double fine = libraryService.returnBook(bId, member.getId());
            Book b = libraryService.getBookById(bId);
            String title = b != null ? b.getTitle() : "Book #" + bId;
            System.out.println("\n[SUCCESS] \"" + title + "\" returned successfully!");
            if (fine > 0) {
                System.out.printf("[PENALTY] Book was returned overdue. Late fee applied: Rs. %.2f%n", fine);
            } else {
                System.out.println("[ON TIME] Returned on or before due date. Zero fine applied!");
            }
        } catch (LibraryException e) {
            System.out.println("\n[RETURN FAILED] " + e.getMessage());
        }
    }

    private static void displayMemberHistory(Member member) {
        printHeader("MY TRANSACTION HISTORY");
        List<Transaction> myTxList = libraryService.getMemberTransactions(member.getId());
        if (myTxList.isEmpty()) {
            System.out.println("You have not made any borrow transactions yet.");
            return;
        }

        String today = LocalDate.now().toString();
        System.out.printf("%-5s | %-7s | %-11s | %-11s | %-11s | %-10s | %-8s%n",
                "TX ID", "BOOK ID", "ISSUE DATE", "DUE DATE", "RETURN DATE", "STATUS", "FINE");
        System.out.println("---------------------------------------------------------------------------------------");
        for (Transaction tx : myTxList) {
            String retStr = tx.getReturnDate() == null ? "-" : tx.getReturnDate();
            String status = tx.getReturnDate() != null ? "RETURNED" : (tx.isOverdue(today) ? "OVERDUE" : "ACTIVE");
            System.out.printf("%-5d | %-7d | %-11s | %-11s | %-11s | %-10s | Rs. %-6.2f%n",
                    tx.getId(), tx.getBookId(), tx.getIssueDate(), tx.getDueDate(), retStr, status, tx.getFine());
        }
        System.out.println("=======================================================================================");
    }
}
