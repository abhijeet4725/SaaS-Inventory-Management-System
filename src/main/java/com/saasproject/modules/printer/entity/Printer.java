package com.saasproject.modules.printer.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Registered printer entity for thermal/label printers.
 */
@Entity
@Table(name = "printers", indexes = {
        @Index(name = "idx_printers_tenant", columnList = "tenantId"),
        @Index(name = "idx_printers_default", columnList = "tenantId, isDefault")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Printer extends BaseEntity {

    public enum PrinterType {
        THERMAL_58MM, // 58mm thermal receipt printer
        THERMAL_80MM, // 80mm thermal receipt printer
        LABEL, // Label/barcode printer
        NORMAL // Standard printer
    }

    public enum ConnectionType {
        NETWORK, // Network printer (IP:Port)
        USB, // USB connected
        BLUETOOTH // Bluetooth connected
    }

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "printer_type", nullable = false, length = 30)
    @Builder.Default
    private PrinterType printerType = PrinterType.THERMAL_80MM;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, length = 20)
    @Builder.Default
    private ConnectionType connectionType = ConnectionType.NETWORK;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column
    @Builder.Default
    private Integer port = 9100;

    @Column(name = "usb_path", length = 255)
    private String usbPath;

    @Column(name = "paper_width")
    @Builder.Default
    private Integer paperWidth = 80; // mm

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column
    @Builder.Default
    private Boolean active = true;

    /**
     * Get the number of characters per line based on paper width.
     */
    public int getCharsPerLine() {
        return switch (paperWidth) {
            case 58 -> 32;
            case 80 -> 48;
            default -> 48;
        };
    }
}
