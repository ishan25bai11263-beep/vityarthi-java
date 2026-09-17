import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryStorage {
    private static final String BOOKS_FILE = "data/books.txt";
    private static final String MEMBERS_FILE = "data/members.txt";
    private static final String TRANSACTIONS_FILE = "data/transactions.txt";

    public LibraryStorage() {
        initStorage();
    }

    private synchronized void initStorage() {
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File mFile = new File(MEMBERS_FILE);
        if (!mFile.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(mFile))) {
                bw.write("1|System Admin|admin@library.com|admin123|ADMIN|0\n");
                bw.write("2|Rahul Sharma|rahul@student.edu|pass123|MEMBER|0\n");
                bw.write("3|Priya Patel|priya@student.edu|pass123|MEMBER|0\n");
            } catch (IOException e) {
                System.err.println("Failed to initialize members file: " + e.getMessage());
            }
        }

        File bFile = new File(BOOKS_FILE);
        if (!bFile.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(bFile))) {
                bw.write("1|Core Java Volume I|Cay S. Horstmann|978-0135166307|AVAILABLE\n");
                bw.write("2|Head First Java|Kathy Sierra|978-0596009205|AVAILABLE\n");
                bw.write("3|Effective Java|Joshua Bloch|978-0134685991|AVAILABLE\n");
                bw.write("4|Introduction to Algorithms|Thomas H. Cormen|978-0262033848|AVAILABLE\n");
            } catch (IOException e) {
                System.err.println("Failed to initialize books file: " + e.getMessage());
            }
        }

        File tFile = new File(TRANSACTIONS_FILE);
        if (!tFile.exists()) {
            try {
                tFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Failed to initialize transactions file: " + e.getMessage());
            }
        }
    }

    public synchronized List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        File file = new File(BOOKS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    String author = parts[2];
                    String isbn = parts[3];
                    BookStatus status = BookStatus.valueOf(parts[4]);
                    list.add(new Book(id, title, author, isbn, status));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading books file: " + e.getMessage());
        }
        return list;
    }

    public synchronized boolean addBook(Book book) {
        List<Book> books = getAllBooks();
        int maxId = 0;
        String cleanIsbn = book.getIsbn().trim();
        for (Book b : books) {
            if (b.getId() > maxId) maxId = b.getId();
            if (b.getIsbn().trim().equalsIgnoreCase(cleanIsbn)) {
                return false;
            }
        }
        book.setId(maxId + 1);
        books.add(book);
        return saveAllBooks(books);
    }

    public synchronized boolean updateBook(Book book) {
        List<Book> books = getAllBooks();
        boolean found = false;
        String cleanIsbn = book.getIsbn().trim();
        for (int i = 0; i < books.size(); i++) {
            Book existing = books.get(i);
            if (existing.getId() != book.getId() && existing.getIsbn().trim().equalsIgnoreCase(cleanIsbn)) {
                return false;
            }
            if (existing.getId() == book.getId()) {
                books.set(i, book);
                found = true;
            }
        }
        return found && saveAllBooks(books);
    }

    public synchronized boolean deleteBook(int id) {
        List<Book> books = getAllBooks();
        boolean removed = books.removeIf(b -> b.getId() == id);
        return removed && saveAllBooks(books);
    }

    public synchronized Book getBookById(int id) {
        for (Book b : getAllBooks()) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    public synchronized Book getBookByIsbn(String isbn) {
        if (isbn == null) return null;
        String clean = isbn.trim();
        for (Book b : getAllBooks()) {
            if (b.getIsbn().trim().equalsIgnoreCase(clean)) return b;
        }
        return null;
    }

    private synchronized boolean saveAllBooks(List<Book> books) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_FILE, false))) {
            for (Book b : books) {
                bw.write(b.getId() + "|" + b.getTitle() + "|" + b.getAuthor() + "|" + b.getIsbn() + "|" + b.getStatus().name() + "\n");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving books file: " + e.getMessage());
            return false;
        }
    }

    public synchronized List<Member> getAllMembers() {
        List<Member> list = new ArrayList<>();
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 6 && "MEMBER".equalsIgnoreCase(p[4])) {
                    list.add(new Member(
                            Integer.parseInt(p[0]),
                            p[1],
                            p[2],
                            p[3],
                            Integer.parseInt(p[5])
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading members: " + e.getMessage());
        }
        return list;
    }

    public synchronized boolean addMember(Member member) {
        File file = new File(MEMBERS_FILE);
        int maxId = 0;
        List<String> rawLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                rawLines.add(line);
                String[] p = line.split("\\|");
                int id = Integer.parseInt(p[0]);
                if (id > maxId) maxId = id;
                if (p[2].equalsIgnoreCase(member.getEmail())) {
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking members: " + e.getMessage());
        }

        member.setId(maxId + 1);
        String newLine = member.getId() + "|" + member.getName() + "|" + member.getEmail() + "|" +
                member.getPassword() + "|MEMBER|" + member.getBorrowedCount();
        rawLines.add(newLine);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEMBERS_FILE, false))) {
            for (String l : rawLines) {
                bw.write(l + "\n");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing new member: " + e.getMessage());
            return false;
        }
    }

    public synchronized Member getMemberById(int id) {
        for (Member m : getAllMembers()) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    public synchronized User authenticate(String email, String password) {
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 5) {
                    if (p[2].equalsIgnoreCase(email) && p[3].equals(password)) {
                        int id = Integer.parseInt(p[0]);
                        String name = p[1];
                        String role = p[4];
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            return new Admin(id, name, p[2], p[3]);
                        } else {
                            int count = p.length >= 6 ? Integer.parseInt(p[5]) : 0;
                            return new Member(id, name, p[2], p[3], count);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return null;
    }

    public synchronized boolean updateBorrowedCount(int memberId, int count) {
        File file = new File(MEMBERS_FILE);
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (Integer.parseInt(p[0]) == memberId) {
                    p[5] = String.valueOf(count);
                    lines.add(String.join("|", p));
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            return false;
        }

        if (updated) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEMBERS_FILE, false))) {
                for (String l : lines) {
                    bw.write(l + "\n");
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public synchronized List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 7) {
                    int id = Integer.parseInt(p[0]);
                    int bId = Integer.parseInt(p[1]);
                    int mId = Integer.parseInt(p[2]);
                    String issue = p[3];
                    String due = p[4];
                    String ret = "null".equalsIgnoreCase(p[5]) ? null : p[5];
                    double fine = Double.parseDouble(p[6]);
                    list.add(new Transaction(id, bId, mId, issue, due, ret, fine));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
        return list;
    }

    public synchronized boolean addTransaction(Transaction tx) {
        List<Transaction> all = getAllTransactions();
        int maxId = 0;
        for (Transaction t : all) {
            if (t.getId() > maxId) maxId = t.getId();
        }
        tx.setId(maxId + 1);
        all.add(tx);
        return saveAllTransactions(all);
    }

    public synchronized boolean updateReturn(int txId, String returnDate, double fine) {
        List<Transaction> all = getAllTransactions();
        boolean found = false;
        for (Transaction t : all) {
            if (t.getId() == txId) {
                t.setReturnDate(returnDate);
                t.setFine(fine);
                found = true;
                break;
            }
        }
        return found && saveAllTransactions(all);
    }

    public synchronized Transaction getActiveTransaction(int bookId, int memberId) {
        for (Transaction t : getAllTransactions()) {
            if (t.getBookId() == bookId && t.getMemberId() == memberId && t.getReturnDate() == null) {
                return t;
            }
        }
        return null;
    }

    public synchronized List<Transaction> getActiveTransactions() {
        List<Transaction> active = new ArrayList<>();
        for (Transaction t : getAllTransactions()) {
            if (t.getReturnDate() == null) {
                active.add(t);
            }
        }
        return active;
    }

    public synchronized List<Transaction> getTransactionsByMember(int memberId) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : getAllTransactions()) {
            if (t.getMemberId() == memberId) {
                list.add(t);
            }
        }
        return list;
    }

    public synchronized List<Transaction> getTransactionsByBook(int bookId) {
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : getAllTransactions()) {
            if (t.getBookId() == bookId) {
                list.add(t);
            }
        }
        return list;
    }

    public synchronized double getTotalFinesByMember(int memberId) {
        double total = 0.0;
        for (Transaction t : getAllTransactions()) {
            if (t.getMemberId() == memberId) {
                total += t.getFine();
            }
        }
        return total;
    }

    private synchronized boolean saveAllTransactions(List<Transaction> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE, false))) {
            for (Transaction t : list) {
                String ret = t.getReturnDate() == null ? "null" : t.getReturnDate();
                bw.write(t.getId() + "|" + t.getBookId() + "|" + t.getMemberId() + "|" +
                        t.getIssueDate() + "|" + t.getDueDate() + "|" + ret + "|" + t.getFine() + "\n");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
            return false;
        }
    }
}
