package com.saasproject.modules.printer.service;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.saasproject.modules.invoice.entity.Invoice;
import com.saasproject.modules.invoice.entity.InvoiceItem;
import com.saasproject.modules.pos.entity.Cart;
import com.saasproject.modules.pos.entity.CartItem;
import com.saasproject.modules.printer.entity.Printer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating ESC/POS commands using escpos-coffee library.
 */
@Slf4j
@Service
public class EscPosService {

    @Value("${app.company.name:My Store}")
    private String companyName;

    @Value("${app.company.address:123 Main Street}")
    private String companyAddress;

    @Value("${app.company.phone:+1234567890}")
    private String companyPhone;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Generate receipt bytes for an invoice.
     */
    public byte[] generateInvoiceReceipt(Invoice invoice, int paperWidth) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (EscPos escpos = new EscPos(baos)) {
            int charsPerLine = paperWidth == 58 ? 32 : 48;

            // Styles
            Style titleStyle = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true);

            Style headerStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            Style boldStyle = new Style().setBold(true);
            Style rightStyle = new Style().setJustification(EscPosConst.Justification.Right);

            // Company Header
            escpos.writeLF(titleStyle, companyName);
            escpos.writeLF(headerStyle, companyAddress);
            escpos.writeLF(headerStyle, "Tel: " + companyPhone);
            escpos.feed(1);

            // Separator
            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.feed(1);

            // Invoice Info
            escpos.writeLF(boldStyle, "INVOICE: " + invoice.getInvoiceNumber());
            escpos.writeLF("Date: " + invoice.getInvoiceDate().format(DATE_FORMAT));
            if (invoice.getCustomerName() != null) {
                escpos.writeLF("Customer: " + invoice.getCustomerName());
            }
            escpos.feed(1);

            // Separator
            escpos.writeLF(repeatChar('-', charsPerLine));

            // Items Header
            escpos.writeLF(formatLine("Item", "Qty", "Amount", charsPerLine));
            escpos.writeLF(repeatChar('-', charsPerLine));

            // Items
            for (InvoiceItem item : invoice.getItems()) {
                String itemName = truncate(item.getProductName(), charsPerLine - 20);
                String qty = String.valueOf(item.getQuantity());
                String amount = formatAmount(item.getAmount());
                escpos.writeLF(formatLine(itemName, qty, amount, charsPerLine));
            }

            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.feed(1);

            // Totals
            escpos.writeLF(formatTotalLine("Subtotal:", formatAmount(invoice.getSubtotal()), charsPerLine));
            if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0) {
                escpos.writeLF(formatTotalLine("Tax:", formatAmount(invoice.getTaxAmount()), charsPerLine));
            }
            if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().doubleValue() > 0) {
                escpos.writeLF(
                        formatTotalLine("Discount:", "-" + formatAmount(invoice.getDiscountAmount()), charsPerLine));
            }
            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.writeLF(boldStyle, formatTotalLine("TOTAL:", formatAmount(invoice.getTotalAmount()), charsPerLine));

            // Payment Info
            if (invoice.getPaidAmount() != null && invoice.getPaidAmount().doubleValue() > 0) {
                escpos.feed(1);
                escpos.writeLF(formatTotalLine("Paid:", formatAmount(invoice.getPaidAmount()), charsPerLine));
                if (invoice.getBalanceDue() != null && invoice.getBalanceDue().doubleValue() > 0) {
                    escpos.writeLF(formatTotalLine("Balance:", formatAmount(invoice.getBalanceDue()), charsPerLine));
                }
            }

            escpos.feed(1);
            escpos.writeLF(repeatChar('-', charsPerLine));

            // Footer
            escpos.feed(1);
            escpos.writeLF(headerStyle, "Thank you for your business!");
            escpos.writeLF(headerStyle, "Please visit again");
            escpos.feed(3);

            // Cut paper
            escpos.cut(EscPos.CutMode.PART);
        }

        return baos.toByteArray();
    }

    /**
     * Generate receipt bytes for a POS cart.
     */
    public byte[] generateCartReceipt(Cart cart, int paperWidth) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (EscPos escpos = new EscPos(baos)) {
            int charsPerLine = paperWidth == 58 ? 32 : 48;

            Style titleStyle = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true);

            Style headerStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            Style boldStyle = new Style().setBold(true);

            // Company Header
            escpos.writeLF(titleStyle, companyName);
            escpos.writeLF(headerStyle, companyAddress);
            escpos.writeLF(headerStyle, "Tel: " + companyPhone);
            escpos.feed(1);

            // Receipt Info
            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.writeLF(boldStyle, "RECEIPT");
            escpos.writeLF("Date: " + cart.getCreatedAt().format(DATE_FORMAT));
            if (cart.getCustomerName() != null) {
                escpos.writeLF("Customer: " + cart.getCustomerName());
            }
            escpos.writeLF(repeatChar('-', charsPerLine));

            // Items Header
            escpos.writeLF(formatLine("Item", "Qty", "Amount", charsPerLine));
            escpos.writeLF(repeatChar('-', charsPerLine));

            // Items
            for (CartItem item : cart.getItems()) {
                String itemName = truncate(item.getProductName(), charsPerLine - 20);
                String qty = String.valueOf(item.getQuantity());
                String amount = formatAmount(item.getAmount());
                escpos.writeLF(formatLine(itemName, qty, amount, charsPerLine));
            }

            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.feed(1);

            // Totals
            escpos.writeLF(formatTotalLine("Subtotal:", formatAmount(cart.getSubtotal()), charsPerLine));
            if (cart.getTaxAmount() != null && cart.getTaxAmount().doubleValue() > 0) {
                escpos.writeLF(formatTotalLine("Tax:", formatAmount(cart.getTaxAmount()), charsPerLine));
            }
            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.writeLF(boldStyle, formatTotalLine("TOTAL:", formatAmount(cart.getTotalAmount()), charsPerLine));

            // Payment Method
            if (cart.getPaymentMethod() != null) {
                escpos.feed(1);
                escpos.writeLF("Payment: " + cart.getPaymentMethod());
            }

            escpos.feed(1);
            escpos.writeLF(repeatChar('-', charsPerLine));

            // Footer
            escpos.feed(1);
            escpos.writeLF(headerStyle, "Thank you!");
            escpos.writeLF(headerStyle, "Please visit again");
            escpos.feed(3);

            // Cut paper
            escpos.cut(EscPos.CutMode.PART);
        }

        return baos.toByteArray();
    }

    /**
     * Generate a test print.
     */
    public byte[] generateTestPrint(String message, int paperWidth) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (EscPos escpos = new EscPos(baos)) {
            int charsPerLine = paperWidth == 58 ? 32 : 48;

            Style titleStyle = new Style()
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true);

            Style centerStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            escpos.writeLF(titleStyle, "PRINTER TEST");
            escpos.feed(1);
            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.feed(1);
            escpos.writeLF(centerStyle, message != null ? message : "Printer is working!");
            escpos.feed(1);
            escpos.writeLF(repeatChar('-', charsPerLine));
            escpos.feed(3);
            escpos.cut(EscPos.CutMode.PART);
        }

        return baos.toByteArray();
    }

    /**
     * Send bytes to network printer.
     */
    public void sendToNetworkPrinter(String ipAddress, int port, byte[] data) throws IOException {
        log.info("Sending {} bytes to printer at {}:{}", data.length, ipAddress, port);
        try (Socket socket = new Socket(ipAddress, port);
                OutputStream os = socket.getOutputStream()) {
            os.write(data);
            os.flush();
            log.info("Print data sent successfully");
        }
    }

    /**
     * Send bytes to USB printer by printer name.
     * Uses Java Print Service API with escpos-coffee PrinterOutputStream.
     */
    public void sendToUsbPrinter(String printerName, byte[] data) throws IOException {
        log.info("Sending {} bytes to USB printer: {}", data.length, printerName);

        javax.print.PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
        if (printService == null) {
            throw new IOException("Printer not found: " + printerName);
        }

        try (PrinterOutputStream pos = new PrinterOutputStream(printService)) {
            pos.write(data);
            pos.flush();
            log.info("USB print data sent successfully to: {}", printerName);
        }
    }

    /**
     * Get list of available system printers (USB/Local printers).
     * Returns printer names that can be used with sendToUsbPrinter.
     */
    public List<SystemPrinter> discoverSystemPrinters() {
        log.info("Discovering system printers...");
        List<SystemPrinter> printers = new ArrayList<>();

        javax.print.PrintService[] printServices = javax.print.PrintServiceLookup.lookupPrintServices(null, null);

        for (javax.print.PrintService service : printServices) {
            String name = service.getName();
            boolean isDefault = service.equals(javax.print.PrintServiceLookup.lookupDefaultPrintService());

            // Check if it supports raw printing (thermal printers typically do)
            boolean supportsRaw = service.isDocFlavorSupported(
                    javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE);

            printers.add(new SystemPrinter(name, isDefault, supportsRaw, "USB/LOCAL"));
            log.debug("Found printer: {} (default={}, raw={})", name, isDefault, supportsRaw);
        }

        log.info("Discovered {} system printers", printers.size());
        return printers;
    }

    /**
     * Get the default system printer.
     */
    public SystemPrinter getDefaultSystemPrinter() {
        javax.print.PrintService defaultService = javax.print.PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService == null) {
            return null;
        }

        boolean supportsRaw = defaultService.isDocFlavorSupported(
                javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE);

        return new SystemPrinter(defaultService.getName(), true, supportsRaw, "USB/LOCAL");
    }

    /**
     * Check if a specific printer exists on the system.
     */
    public boolean isPrinterAvailable(String printerName) {
        javax.print.PrintService service = PrinterOutputStream.getPrintServiceByName(printerName);
        return service != null;
    }

    /**
     * Send data to printer based on connection type.
     */
    public void sendToPrinter(com.saasproject.modules.printer.entity.Printer printer, byte[] data) throws IOException {
        switch (printer.getConnectionType()) {
            case NETWORK -> sendToNetworkPrinter(printer.getIpAddress(), printer.getPort(), data);
            case USB -> sendToUsbPrinter(printer.getUsbPath(), data);
            case BLUETOOTH -> throw new IOException("Bluetooth printing not yet implemented");
            default -> throw new IOException("Unknown connection type: " + printer.getConnectionType());
        }
    }

    /**
     * DTO for discovered system printers.
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SystemPrinter {
        private String name;
        private boolean isDefault;
        private boolean supportsRaw; // True for thermal printers
        private String connectionType;
    }

    /**
     * Generate text preview of receipt (for display in UI).
     */
    public List<String> generateReceiptPreviewLines(Invoice invoice, int paperWidth) {
        int charsPerLine = paperWidth == 58 ? 32 : 48;
        List<String> lines = new ArrayList<>();

        lines.add(center(companyName, charsPerLine));
        lines.add(center(companyAddress, charsPerLine));
        lines.add(center("Tel: " + companyPhone, charsPerLine));
        lines.add("");
        lines.add(repeatChar('-', charsPerLine));
        lines.add("INVOICE: " + invoice.getInvoiceNumber());
        lines.add("Date: " + invoice.getInvoiceDate().format(DATE_FORMAT));
        if (invoice.getCustomerName() != null) {
            lines.add("Customer: " + invoice.getCustomerName());
        }
        lines.add(repeatChar('-', charsPerLine));
        lines.add(formatLine("Item", "Qty", "Amount", charsPerLine));
        lines.add(repeatChar('-', charsPerLine));

        for (InvoiceItem item : invoice.getItems()) {
            String itemName = truncate(item.getProductName(), charsPerLine - 20);
            String qty = String.valueOf(item.getQuantity());
            String amount = formatAmount(item.getAmount());
            lines.add(formatLine(itemName, qty, amount, charsPerLine));
        }

        lines.add(repeatChar('-', charsPerLine));
        lines.add(formatTotalLine("Subtotal:", formatAmount(invoice.getSubtotal()), charsPerLine));
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0) {
            lines.add(formatTotalLine("Tax:", formatAmount(invoice.getTaxAmount()), charsPerLine));
        }
        lines.add(repeatChar('-', charsPerLine));
        lines.add(formatTotalLine("TOTAL:", formatAmount(invoice.getTotalAmount()), charsPerLine));
        lines.add("");
        lines.add(center("Thank you for your business!", charsPerLine));

        return lines;
    }

    // ===== Helper Methods =====

    private String repeatChar(char c, int count) {
        return String.valueOf(c).repeat(count);
    }

    private String truncate(String text, int maxLen) {
        if (text == null)
            return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 2) + ".." : text;
    }

    private String formatAmount(java.math.BigDecimal amount) {
        if (amount == null)
            return "0.00";
        return String.format("%.2f", amount);
    }

    private String formatLine(String name, String qty, String amount, int lineWidth) {
        int qtyWidth = 5;
        int amtWidth = 10;
        int nameWidth = lineWidth - qtyWidth - amtWidth;

        String namePart = truncate(name, nameWidth);
        return String.format("%-" + nameWidth + "s%" + qtyWidth + "s%" + amtWidth + "s", namePart, qty, amount);
    }

    private String formatTotalLine(String label, String amount, int lineWidth) {
        int amtWidth = 12;
        int labelWidth = lineWidth - amtWidth;
        return String.format("%-" + labelWidth + "s%" + amtWidth + "s", label, amount);
    }

    private String center(String text, int width) {
        if (text == null || text.length() >= width)
            return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
}
