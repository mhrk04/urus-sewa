package carrental.service;

import carrental.model.Car;
import carrental.model.Rental;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates a downloadable PDF invoice for a rental transaction.
 *
 * Uses raw PDF format writing (no external libraries required).
 * PDF 1.4 specification – creates a single-page document with
 * the rental receipt details formatted as a professional invoice.
 *
 * This is a bonus feature: "export to file / reporting"
 * as described in the rubric for extra marks.
 *
 * @author  Group 11 – UniKL MIIT ISB16003
 */
public class InvoiceService {

    /** Directory where invoices are saved (project root). */
    private static final String INVOICE_DIR = "invoices";

    /**
     * Generates a PDF invoice file for the given rental transaction.
     *
     * @param rental the rental record
     * @param car    the rented car
     * @return the file path of the generated PDF, or null on failure
     */
    public String generateInvoice(Rental rental, Car car) {
        // Ensure invoices directory exists
        java.io.File dir = new java.io.File(INVOICE_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = INVOICE_DIR + "/" + rental.getRentalId() + "_invoice.pdf";

        try {
            // Build the invoice text content
            String content = buildInvoiceContent(rental, car);
            writePdf(fileName, content);
            return fileName;
        } catch (IOException e) {
            System.err.println("Error generating invoice: " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds the formatted invoice text content.
     *
     * @param rental the rental record with customer info
     * @param car    the rented car details
     * @return multi-line invoice text
     */
    private String buildInvoiceContent(Rental rental, Car car) {
        StringBuilder sb = new StringBuilder();

        sb.append("URUSSEWA - CAR RENTAL INVOICE");
        sb.append("\n");
        sb.append("=====================================");
        sb.append("\n\n");

        sb.append("Invoice No    : ").append(rental.getRentalId()).append("\n");
        sb.append("Rent Date     : ").append(rental.getRentalDate()).append("\n");
        sb.append("Return Date   : ").append(rental.getReturnDate()).append("\n");
        sb.append("\n");

        sb.append("-------------------------------------");
        sb.append("\n");
        sb.append("CUSTOMER DETAILS");
        sb.append("\n");
        sb.append("-------------------------------------");
        sb.append("\n");
        sb.append("Name          : ").append(rental.getCustomerName()).append("\n");
        sb.append("IC / Passport : ").append(rental.getCustomerIC()).append("\n");
        sb.append("Phone         : ").append(rental.getCustomerPhone()).append("\n");
        sb.append("\n");

        sb.append("-------------------------------------");
        sb.append("\n");
        sb.append("VEHICLE DETAILS");
        sb.append("\n");
        sb.append("-------------------------------------");
        sb.append("\n");
        sb.append("Car ID        : ").append(car.getId()).append("\n");
        sb.append("Vehicle       : ").append(car.getBrand()).append(" ").append(car.getModel()).append("\n");
        sb.append("Type          : ").append(car.getCarType()).append("\n");
        sb.append("Rate / Day    : RM ").append(String.format("%.2f", car.getPricePerDay())).append("\n");
        sb.append("\n");

        sb.append("-------------------------------------");
        sb.append("\n");
        sb.append("RENTAL SUMMARY");
        sb.append("\n");
        sb.append("-------------------------------------");
        sb.append("\n");
        sb.append("Duration      : ").append(rental.getRentalDays()).append(" day(s)").append("\n");
        sb.append("Base Cost     : RM ").append(String.format("%.2f", car.getPricePerDay() * rental.getRentalDays())).append("\n");

        // Show surcharge if applicable
        double baseCost = car.getPricePerDay() * rental.getRentalDays();
        double surcharge = rental.getTotalCost() - baseCost;
        if (surcharge > 0.01) {
            sb.append("Surcharge     : RM ").append(String.format("%.2f", surcharge)).append("\n");
        }

        sb.append("\n");
        sb.append("=====================================");
        sb.append("\n");
        sb.append("TOTAL COST    : RM ").append(String.format("%.2f", rental.getTotalCost()));
        sb.append("\n");
        sb.append("=====================================");
        sb.append("\n\n");

        sb.append("Thank you for choosing UrusSewa!");
        sb.append("\n");
        sb.append("UniKL MIIT - Group 11 | ISB16003");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Writes a text string into a valid PDF 1.4 file.
     *
     * Creates a minimal but valid PDF document structure:
     *   - Catalog (root object)
     *   - Pages dictionary
     *   - Single Page with A4 dimensions
     *   - Content stream with text operators
     *   - Font resource (Helvetica, built-in PDF font)
     *   - Cross-reference table and trailer
     *
     * No external PDF libraries are used.
     *
     * @param filePath output PDF file path
     * @param text     the text content to render
     * @throws IOException if file writing fails
     */
    private void writePdf(String filePath, String text) throws IOException {
        // Split text into lines for PDF rendering
        String[] lines = text.split("\n");

        // Build content stream (PDF text operators)
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n");                      // Begin Text
        stream.append("/F1 11 Tf\n");               // Font: Helvetica, 11pt
        stream.append("50 750 Td\n");               // Starting position (x=50, y=750)
        stream.append("14 TL\n");                   // Line spacing: 14pt

        for (String line : lines) {
            // Escape special PDF characters: backslash, parentheses
            String escaped = line.replace("\\", "\\\\")
                                 .replace("(", "\\(")
                                 .replace(")", "\\)");
            stream.append("(").append(escaped).append(") '\n"); // Show text and move to next line
        }
        stream.append("ET\n");                      // End Text

        String streamContent = stream.toString();
        byte[] streamBytes = streamContent.getBytes(StandardCharsets.US_ASCII);

        // Build PDF objects
        // The implementation below constructs a minimal valid PDF file by
        // assembling the common PDF objects in order. We record byte offsets
        // so the cross-reference table (`xref`) can point to each object.
        // Note: This is a light-weight approach intended for simple textual
        // invoices and avoids pulling in an external PDF library.
        // Object 1: Catalog
        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";

        // Object 2: Pages
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";

        // Object 3: Page (A4 size: 595 x 842 points)
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R "
                + "/MediaBox [0 0 595 842] "
                + "/Contents 4 0 R "
                + "/Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n";

        // Object 4: Content stream
        String obj4 = "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n"
                + streamContent + "endstream\nendobj\n";

        // Object 5: Font (Courier used as a built-in Type1 font)
        // Built-in fonts (Courier, Helvetica, Times-Roman) do not require
        // embedding and are safe to use for simple text-only PDFs.
        String obj5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n";

        // Assemble full PDF
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        // Track byte offsets for cross-reference table
        int[] offsets = new int[5];

        offsets[0] = pdf.length();
        pdf.append(obj1);

        offsets[1] = pdf.length();
        pdf.append(obj2);

        offsets[2] = pdf.length();
        pdf.append(obj3);

        offsets[3] = pdf.length();
        pdf.append(obj4);

        offsets[4] = pdf.length();
        pdf.append(obj5);

        // Cross-reference table
        int xrefOffset = pdf.length();
        pdf.append("xref\n");
        pdf.append("0 6\n");
        pdf.append("0000000000 65535 f \n"); // Free entry
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }

        // Trailer
        pdf.append("trailer\n");
        pdf.append("<< /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n");
        pdf.append(xrefOffset).append("\n");
        pdf.append("%%EOF\n");

        // Write to file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdf.toString().getBytes(StandardCharsets.US_ASCII));
        }
    }
}
