import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {
    private static final String LOG_FILE = "data/library.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String action, String details) {
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String threadName = Thread.currentThread().getName();
            String logEntry = String.format("[%s] [%s] [%s] %s%n", timestamp, threadName, action.toUpperCase(), details);
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public static boolean exportDetailedReport(List<Transaction> transactions, List<Book> books, List<Member> members, String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String todayStr = LocalDate.now().toString();
            String genTimestamp = LocalDateTime.now().format(FORMATTER);

            int totalTx = transactions.size();
            int activeCount = 0;
            int overdueCount = 0;
            double totalFines = 0.0;
            Map<Integer, Integer> bookBorrowCounts = new HashMap<>();
            Map<Integer, Double> memberFines = new HashMap<>();

            for (Transaction tx : transactions) {
                totalFines += tx.getFine();
                bookBorrowCounts.put(tx.getBookId(), bookBorrowCounts.getOrDefault(tx.getBookId(), 0) + 1);

                if (tx.getReturnDate() == null) {
                    activeCount++;
                    if (tx.isOverdue(todayStr)) {
                        overdueCount++;
                    }
                }

                if (tx.getFine() > 0) {
                    memberFines.put(tx.getMemberId(), memberFines.getOrDefault(tx.getMemberId(), 0.0) + tx.getFine());
                }
            }

            int topBookId = -1;
            int maxBorrows = 0;
            for (Map.Entry<Integer, Integer> entry : bookBorrowCounts.entrySet()) {
                if (entry.getValue() > maxBorrows) {
                    maxBorrows = entry.getValue();
                    topBookId = entry.getKey();
                }
            }

            String topBookTitle = "None";
            for (Book b : books) {
                if (b.getId() == topBookId) {
                    topBookTitle = "\"" + b.getTitle() + "\" (" + maxBorrows + " times borrowed)";
                    break;
                }
            }

            writer.write("========================================================================================\n");
            writer.write("                           SMART LIBRARY MANAGEMENT SYSTEM                              \n");
            writer.write("                         AUDIT & TRANSACTION SUMMARY REPORT                             \n");
            writer.write("Generated on : " + genTimestamp + "\n");
            writer.write("========================================================================================\n\n");

            writer.write("--- 1. EXECUTIVE SUMMARY ---\n");
            writer.write(String.format("Total Books in Catalog     : %d%n", books.size()));
            writer.write(String.format("Total Registered Members   : %d%n", members.size()));
            writer.write(String.format("Total Transaction Records  : %d%n", totalTx));
            writer.write(String.format("Active (Ongoing) Loans     : %d%n", activeCount));
            writer.write(String.format("Overdue Loans (Action Req.): %d%n", overdueCount));
            writer.write(String.format("Total Fines Collected/Due  : Rs. %.2f%n", totalFines));
            writer.write(String.format("Most Popular Book          : %s%n%n", topBookTitle));

            writer.write("--- 2. MEMBER FINE LEDGER (Members with Fines) ---\n");
            if (memberFines.isEmpty()) {
                writer.write("No penalties or fines recorded across all member accounts.\n\n");
            } else {
                writer.write(String.format("%-10s | %-25s | %-12s%n", "MEMBER ID", "MEMBER NAME", "TOTAL FINES"));
                writer.write("----------------------------------------------------\n");
                for (Map.Entry<Integer, Double> entry : memberFines.entrySet()) {
                    String mName = "Member #" + entry.getKey();
                    for (Member m : members) {
                        if (m.getId() == entry.getKey()) {
                            mName = m.getName();
                            break;
                        }
                    }
                    writer.write(String.format("%-10d | %-25s | Rs. %-9.2f%n", entry.getKey(), mName, entry.getValue()));
                }
                writer.write("\n");
            }

            writer.write("--- 3. COMPLETE TRANSACTION HISTORY ---\n");
            writer.write(String.format("%-6s | %-8s | %-10s | %-12s | %-12s | %-12s | %-10s | %-8s%n",
                    "TX ID", "BOOK ID", "MEMBER ID", "ISSUE DATE", "DUE DATE", "RETURN DATE", "STATUS", "FINE"));
            writer.write("------------------------------------------------------------------------------------------------\n");

            for (Transaction tx : transactions) {
                String retDate = tx.getReturnDate() == null ? "-" : tx.getReturnDate();
                String status;
                if (tx.getReturnDate() != null) {
                    status = "RETURNED";
                } else if (tx.isOverdue(todayStr)) {
                    status = "OVERDUE";
                } else {
                    status = "ACTIVE";
                }
                String fineStr = String.format("Rs. %.2f", tx.getFine());

                writer.write(String.format("%-6d | %-8d | %-10d | %-12s | %-12s | %-12s | %-10s | %-8s%n",
                    tx.getId(), tx.getBookId(), tx.getMemberId(), tx.getIssueDate(), tx.getDueDate(), retDate, status, fineStr));
            }

            writer.write("------------------------------------------------------------------------------------------------\n");
            writer.write("================================== END OF REPORT =======================================\n");
            return true;
        } catch (IOException e) {
            System.err.println("Error generating report file: " + e.getMessage());
            return false;
        }
    }
}
