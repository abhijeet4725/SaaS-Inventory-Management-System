package com.saasproject.common.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.saasproject.modules.invoice.entity.Invoice;
import com.saasproject.modules.invoice.entity.InvoiceItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating PDF documents using iText.
 */
@Slf4j
@Service
public class PdfGeneratorService {

    @Value("${app.company.name:My Company}")
    private String companyName;

    @Value("${app.company.address:123 Business Street, City, Country}")
    private String companyAddress;

    @Value("${app.company.phone:+1234567890}")
    private String companyPhone;

    @Value("${app.company.email:contact@company.com}")
    private String companyEmail;

    @Value("${app.company.gst:GSTIN123456789}")
    private String companyGst;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(236, 240, 241);

    /**
     * Generate invoice PDF.
     */
    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(40, 40, 40, 40);

            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ===== HEADER =====
            addCompanyHeader(document, boldFont, regularFont);

            // ===== INVOICE TITLE =====
            Paragraph title = new Paragraph("INVOICE")
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(title);

            // ===== INVOICE DETAILS =====
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // Left side - Invoice info
            Cell leftCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber()).setFont(boldFont))
                    .add(new Paragraph("Date: " + invoice.getInvoiceDate().format(DATE_FORMAT)).setFont(regularFont))
                    .add(new Paragraph("Due Date: "
                            + (invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMAT) : "N/A"))
                            .setFont(regularFont))
                    .add(new Paragraph("Status: " + invoice.getStatus()).setFont(regularFont)
                            .setFontColor(getStatusColor(invoice.getStatus())));
            detailsTable.addCell(leftCell);

            // Right side - Customer info
            Cell rightCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Bill To:").setFont(boldFont))
                    .add(new Paragraph(
                            invoice.getCustomerName() != null ? invoice.getCustomerName() : "Walk-in Customer")
                            .setFont(regularFont))
                    .add(new Paragraph(invoice.getCustomerPhone() != null ? invoice.getCustomerPhone() : "")
                            .setFont(regularFont))
                    .add(new Paragraph(invoice.getCustomerEmail() != null ? invoice.getCustomerEmail() : "")
                            .setFont(regularFont));
            detailsTable.addCell(rightCell);

            document.add(detailsTable);

            // ===== ITEMS TABLE =====
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[] { 0.5f, 3f, 1f, 1f, 1f, 1.5f }))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // Table header
            String[] headers = { "#", "Item", "Qty", "Price", "Tax", "Amount" };
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .setBackgroundColor(PRIMARY_COLOR)
                        .setPadding(8)
                        .add(new Paragraph(header).setFont(boldFont).setFontColor(ColorConstants.WHITE)
                                .setFontSize(10));
                itemsTable.addHeaderCell(headerCell);
            }

            // Table rows
            int rowNum = 1;
            boolean alternate = false;
            for (InvoiceItem item : invoice.getItems()) {
                DeviceRgb rowBg = alternate ? HEADER_BG : new DeviceRgb(255, 255, 255);
                alternate = !alternate;

                itemsTable.addCell(createItemCell(String.valueOf(rowNum++), regularFont, rowBg));
                itemsTable.addCell(createItemCell(item.getProductName(), regularFont, rowBg));
                itemsTable.addCell(createItemCell(String.valueOf(item.getQuantity()), regularFont, rowBg));
                itemsTable.addCell(createItemCell(formatCurrency(item.getUnitPrice()), regularFont, rowBg));
                itemsTable.addCell(createItemCell(formatCurrency(item.getTaxAmount()), regularFont, rowBg));
                itemsTable.addCell(
                        createItemCell(formatCurrency(item.getAmount()), regularFont, rowBg, TextAlignment.RIGHT));
            }

            document.add(itemsTable);

            // ===== TOTALS =====
            Table totalsTable = new Table(UnitValue.createPercentArray(new float[] { 3f, 1.5f }))
                    .setWidth(UnitValue.createPercentValue(40))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setMarginBottom(20);

            addTotalRow(totalsTable, "Subtotal:", formatCurrency(invoice.getSubtotal()), regularFont);
            if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "Tax:", formatCurrency(invoice.getTaxAmount()), regularFont);
            }
            if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "Discount:", "-" + formatCurrency(invoice.getDiscountAmount()), regularFont);
            }

            // Grand total with highlight
            Cell totalLabelCell = new Cell()
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setPadding(8)
                    .add(new Paragraph("TOTAL").setFont(boldFont).setFontColor(ColorConstants.WHITE));
            Cell totalValueCell = new Cell()
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph(formatCurrency(invoice.getTotalAmount())).setFont(boldFont)
                            .setFontColor(ColorConstants.WHITE));
            totalsTable.addCell(totalLabelCell);
            totalsTable.addCell(totalValueCell);

            // Payment info
            if (invoice.getPaidAmount() != null && invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "Paid:", formatCurrency(invoice.getPaidAmount()), regularFont);
                if (invoice.getBalanceDue() != null && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
                    addTotalRow(totalsTable, "Balance Due:", formatCurrency(invoice.getBalanceDue()), boldFont);
                }
            }

            document.add(totalsTable);

            // ===== NOTES =====
            if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
                document.add(new Paragraph("Notes:").setFont(boldFont).setMarginTop(20));
                document.add(new Paragraph(invoice.getNotes()).setFont(regularFont).setFontSize(10));
            }

            // ===== FOOTER =====
            addFooter(document, regularFont);

            log.info("Generated PDF for invoice: {}", invoice.getInvoiceNumber());
        }

        return baos.toByteArray();
    }

    // ===== Helper Methods =====

    private void addCompanyHeader(Document document, PdfFont boldFont, PdfFont regularFont) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                .useAllAvailableWidth()
                .setBorderBottom(new SolidBorder(PRIMARY_COLOR, 2))
                .setPaddingBottom(10);

        Cell companyCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(companyName).setFont(boldFont).setFontSize(20).setFontColor(PRIMARY_COLOR))
                .add(new Paragraph(companyAddress).setFont(regularFont).setFontSize(10))
                .add(new Paragraph("Phone: " + companyPhone + " | Email: " + companyEmail).setFont(regularFont)
                        .setFontSize(10))
                .add(new Paragraph("GST: " + companyGst).setFont(regularFont).setFontSize(10));

        headerTable.addCell(companyCell);
        document.add(headerTable);
    }

    private Cell createItemCell(String content, PdfFont font, DeviceRgb bgColor) {
        return createItemCell(content, font, bgColor, TextAlignment.LEFT);
    }

    private Cell createItemCell(String content, PdfFont font, DeviceRgb bgColor, TextAlignment align) {
        return new Cell()
                .setBackgroundColor(bgColor)
                .setPadding(6)
                .setTextAlignment(align)
                .add(new Paragraph(content).setFont(font).setFontSize(10));
    }

    private void addTotalRow(Table table, String label, String value, PdfFont font) {
        Cell labelCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(5)
                .add(new Paragraph(label).setFont(font));
        Cell valueCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(value).setFont(font));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document, PdfFont font) {
        Paragraph footer = new Paragraph("Thank you for your business!")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(30);
        document.add(footer);

        Paragraph terms = new Paragraph("Payment is due within the terms specified above. " +
                "Please include the invoice number with your payment.")
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(terms);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0.00";
        return String.format("%.2f", amount);
    }

    private DeviceRgb getStatusColor(Invoice.InvoiceStatus status) {
        return switch (status) {
            case PAID -> new DeviceRgb(39, 174, 96); // Green
            case PENDING -> new DeviceRgb(241, 196, 15); // Yellow
            case CANCELLED -> new DeviceRgb(231, 76, 60); // Red
            case DRAFT -> new DeviceRgb(149, 165, 166); // Gray
            default -> new DeviceRgb(0, 0, 0);
        };
    }
}
