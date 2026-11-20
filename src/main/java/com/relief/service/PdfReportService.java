package com.relief.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.relief.model.Allocation;
import com.relief.model.Disaster;
import com.relief.model.Need;
import com.relief.model.Pledge;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dedicated service for generating professional, downloadable PDF reports
 * for the Disaster Relief Coordination System using OpenPDF.
 */
@Service
public class PdfReportService {

    private final NeedService needService;
    private final PledgeAllocationService pledgeAllocationService;

    // Theme color palette
    private static final Color PRIMARY_NAVY = new Color(15, 41, 66);        // #0F2942
    private static final Color HEADER_BG = new Color(24, 53, 84);            // #183554
    private static final Color ACCENT_BLUE = new Color(2, 132, 199);         // #0284C7
    private static final Color CARD_BG = new Color(248, 250, 252);           // #F8FAFC
    private static final Color CARD_BORDER = new Color(203, 213, 225);       // #CBD5E1
    private static final Color TEXT_DARK = new Color(15, 23, 42);            // #0F172A
    private static final Color TEXT_MUTED = new Color(100, 116, 139);        // #64748B
    private static final Color ROW_ALT_BG = new Color(248, 250, 252);        // #F8FAFC
    private static final Color ROW_BORDER = new Color(226, 232, 240);        // #E2E8F0

    // Priority badge colors
    private static final Color PRIORITY_CRITICAL = new Color(185, 28, 28);   // #B91C1C
    private static final Color PRIORITY_HIGH = new Color(194, 65, 12);       // #C2410C
    private static final Color PRIORITY_MEDIUM = new Color(29, 78, 216);     // #1D4ED8
    private static final Color PRIORITY_LOW = new Color(71, 85, 105);        // #475569

    // Status colors
    private static final Color STATUS_VERIFIED = new Color(22, 101, 52);     // #166534
    private static final Color STATUS_PENDING = new Color(180, 83, 9);       // #B45309
    private static final Color STATUS_REJECTED = new Color(153, 27, 27);     // #991B1B
    private static final Color STATUS_FULFILLED = new Color(21, 128, 61);    // #15803D
    private static final Color STATUS_PARTIAL = new Color(14, 116, 144);     // #0E7490

    public PdfReportService(NeedService needService, PledgeAllocationService pledgeAllocationService) {
        this.needService = needService;
        this.pledgeAllocationService = pledgeAllocationService;
    }

    /**
     * Generates the department coordination PDF report by fetching live data
     * from the database via NeedService and PledgeAllocationService.
     */
    public byte[] generateDepartmentReport() {
        List<Disaster> disasters = needService.getAllDisasters();
        List<Need> allNeeds = needService.getAllNeeds();
        List<Pledge> pledges = pledgeAllocationService.getAllPledges();
        List<Allocation> allocations = pledgeAllocationService.getAllAllocations();

        return generatePdfReport(disasters, allNeeds, pledges, allocations);
    }

    /**
     * Constructs the PDF binary output given the domain collections.
     */
    public byte[] generatePdfReport(List<Disaster> disasters,
                                   List<Need> allNeeds,
                                   List<Pledge> pledges,
                                   List<Allocation> allocations) {
        if (disasters == null) disasters = Collections.emptyList();
        if (allNeeds == null) allNeeds = Collections.emptyList();
        if (pledges == null) pledges = Collections.emptyList();
        if (allocations == null) allocations = Collections.emptyList();

        Map<Integer, String> disasterMap = disasters.stream()
                .collect(Collectors.toMap(
                        Disaster::getDisasterId,
                        d -> (d.getName() != null && !d.getName().isBlank()) ? d.getName() : "Disaster #" + d.getDisasterId(),
                        (existing, replacement) -> existing
                ));

        // Compute statistics
        int totalDisasters = disasters.size();
        int totalReportedNeeds = allNeeds.size();
        int totalPledges = pledges.size();
        int totalAllocations = allocations.size();

        int totalVerifiedNeeds = 0;
        int totalPendingNeeds = 0;

        for (Need n : allNeeds) {
            String status = safeString(n.getStatus(), "");
            if ("PENDING_VERIFICATION".equalsIgnoreCase(status)) {
                totalPendingNeeds++;
            } else if ("VERIFIED".equalsIgnoreCase(status)
                    || "PARTIALLY_FULFILLED".equalsIgnoreCase(status)
                    || "FULFILLED".equalsIgnoreCase(status)) {
                totalVerifiedNeeds++;
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 32, 32, 36, 42);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            // 1. Report Title & Generation Metadata
            addReportHeader(document);

            // 2. Executive Statistics KPI Cards (Summary)
            addStatisticsGrid(document, totalDisasters, totalReportedNeeds,
                    totalVerifiedNeeds, totalPendingNeeds, totalPledges, totalAllocations);

            // 3. Detailed List of Reported Needs
            addNeedsLedger(document, allNeeds, disasterMap);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addReportHeader(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_NAVY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, ACCENT_BLUE);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED);

        Paragraph title = new Paragraph("Disaster Relief Coordination Report", titleFont);
        title.setSpacingAfter(2f);
        document.add(title);

        Paragraph subtitle = new Paragraph("OFFICIAL RELIEF COMMAND OPERATIONS & FIELD RESOURCE ALLOCATION AUDIT", subtitleFont);
        subtitle.setSpacingAfter(4f);
        document.add(subtitle);

        String generatedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm:ss a"));
        Paragraph meta = new Paragraph("Report Generated: " + generatedTime + "  |  Department Command Center", metaFont);
        meta.setSpacingAfter(12f);
        document.add(meta);

        // Decorative colored divider bar
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100f);
        PdfPCell divCell = new PdfPCell();
        divCell.setBackgroundColor(ACCENT_BLUE);
        divCell.setFixedHeight(2.5f);
        divCell.setBorder(Rectangle.NO_BORDER);
        divider.addCell(divCell);
        divider.setSpacingAfter(14f);
        document.add(divider);
    }

    private void addStatisticsGrid(Document document,
                                  int totalDisasters,
                                  int totalReportedNeeds,
                                  int totalVerifiedNeeds,
                                  int totalPendingNeeds,
                                  int totalPledges,
                                  int totalAllocations) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY_NAVY);
        Paragraph sectionTitle = new Paragraph("Key Operations Summary", sectionFont);
        sectionTitle.setSpacingAfter(6f);
        document.add(sectionTitle);

        PdfPTable grid = new PdfPTable(6);
        grid.setWidthPercentage(100f);
        grid.setWidths(new float[]{16.6f, 16.6f, 16.6f, 16.6f, 16.6f, 17f});

        addKpiCell(grid, "Total Disasters", String.valueOf(totalDisasters), TEXT_DARK);
        addKpiCell(grid, "Reported Needs", String.valueOf(totalReportedNeeds), TEXT_DARK);
        addKpiCell(grid, "Verified Needs", String.valueOf(totalVerifiedNeeds), STATUS_VERIFIED);
        addKpiCell(grid, "Pending Needs", String.valueOf(totalPendingNeeds), STATUS_PENDING);
        addKpiCell(grid, "Total Pledges", String.valueOf(totalPledges), ACCENT_BLUE);
        addKpiCell(grid, "Total Allocations", String.valueOf(totalAllocations), PRIMARY_NAVY);

        grid.setSpacingAfter(18f);
        document.add(grid);
    }

    private void addKpiCell(PdfPTable table, String label, String value, Color valueColor) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, TEXT_MUTED);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, valueColor);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(CARD_BG);
        cell.setBorderColor(CARD_BORDER);
        cell.setBorderWidth(0.75f);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(7f);
        cell.setPaddingLeft(6f);
        cell.setPaddingRight(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLabel = new Paragraph(label.toUpperCase(Locale.ROOT), labelFont);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        pLabel.setSpacingAfter(2f);
        cell.addElement(pLabel);

        Paragraph pValue = new Paragraph(value, valueFont);
        pValue.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pValue);

        table.addCell(cell);
    }

    private void addNeedsLedger(Document document,
                               List<Need> allNeeds,
                               Map<Integer, String> disasterMap) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY_NAVY);
        Paragraph sectionTitle = new Paragraph("Detailed Ledger of Reported Needs (" + allNeeds.size() + " Total)", sectionFont);
        sectionTitle.setSpacingAfter(6f);
        document.add(sectionTitle);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100f);
        // Column widths: ID (5%), Disaster (16%), Category (14%), Description (31%), Urgency (11%), Units (11%), Status (12%)
        table.setWidths(new float[]{5.5f, 16.5f, 14f, 31f, 10.5f, 10.5f, 12f});
        table.setHeaderRows(1);

        // Header cells
        addTableHeaderCell(table, "ID");
        addTableHeaderCell(table, "Related Disaster");
        addTableHeaderCell(table, "Category");
        addTableHeaderCell(table, "Description");
        addTableHeaderCell(table, "Priority");
        addTableHeaderCell(table, "Required");
        addTableHeaderCell(table, "Status");

        if (allNeeds.isEmpty()) {
            Font emptyFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, TEXT_MUTED);
            PdfPCell emptyCell = new PdfPCell(new Phrase("No reported relief needs recorded in the database.", emptyFont));
            emptyCell.setColspan(7);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(14f);
            emptyCell.setBackgroundColor(CARD_BG);
            emptyCell.setBorderColor(ROW_BORDER);
            table.addCell(emptyCell);
        } else {
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, TEXT_DARK);
            Font idFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, PRIMARY_NAVY);

            int rowIndex = 0;
            for (Need n : allNeeds) {
                Color rowBg = (rowIndex % 2 == 1) ? ROW_ALT_BG : Color.WHITE;

                // ID
                PdfPCell idCell = createDataCell("#" + n.getNeedId(), idFont, rowBg, Element.ALIGN_CENTER);
                table.addCell(idCell);

                // Related Disaster
                String disasterName = disasterMap.getOrDefault(n.getDisasterId(),
                        n.getDisasterId() > 0 ? "Disaster #" + n.getDisasterId() : "General Relief");
                table.addCell(createDataCell(disasterName, cellFont, rowBg, Element.ALIGN_LEFT));

                // Category
                table.addCell(createDataCell(safeString(n.getCategory(), "General"), cellFont, rowBg, Element.ALIGN_LEFT));

                // Description
                table.addCell(createDataCell(safeString(n.getDescription(), "No description"), cellFont, rowBg, Element.ALIGN_LEFT));

                // Urgency / Priority (colored badge text)
                String urgency = safeString(n.getUrgency(), "MEDIUM").toUpperCase(Locale.ROOT);
                Color urgColor = switch (urgency) {
                    case "CRITICAL" -> PRIORITY_CRITICAL;
                    case "HIGH" -> PRIORITY_HIGH;
                    case "LOW" -> PRIORITY_LOW;
                    default -> PRIORITY_MEDIUM;
                };
                Font urgFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, urgColor);
                table.addCell(createDataCell(urgency, urgFont, rowBg, Element.ALIGN_CENTER));

                // Quantity breakdown (Req / Alloc / Rem)
                String qtyText = n.getQuantityRequired() + " req" +
                        (n.getQuantityAllocated() > 0 ? "\n(" + n.getQuantityAllocated() + " alloc)" : "");
                table.addCell(createDataCell(qtyText, cellFont, rowBg, Element.ALIGN_CENTER));

                // Status
                String status = safeString(n.getStatus(), "PENDING_VERIFICATION");
                Color statusColor = switch (status.toUpperCase(Locale.ROOT)) {
                    case "VERIFIED" -> STATUS_VERIFIED;
                    case "FULFILLED" -> STATUS_FULFILLED;
                    case "PARTIALLY_FULFILLED" -> STATUS_PARTIAL;
                    case "REJECTED" -> STATUS_REJECTED;
                    default -> STATUS_PENDING;
                };
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, statusColor);
                String displayStatus = formatStatus(status);
                table.addCell(createDataCell(displayStatus, statusFont, rowBg, Element.ALIGN_CENTER));

                rowIndex++;
            }
        }

        document.add(table);
    }

    private void addTableHeaderCell(PdfPTable table, String title) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(title, headerFont));
        cell.setBackgroundColor(HEADER_BG);
        cell.setBorderColor(HEADER_BG);
        cell.setPaddingTop(5f);
        cell.setPaddingBottom(5f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private PdfPCell createDataCell(String text, Font font, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(background);
        cell.setBorderColor(ROW_BORDER);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(4.5f);
        cell.setPaddingBottom(4.5f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private String safeString(String value, String defaultVal) {
        return (value == null || value.isBlank()) ? defaultVal : value.trim();
    }

    private String formatStatus(String status) {
        if (status == null || status.isBlank()) return "Pending";
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "PENDING_VERIFICATION" -> "Pending Verification";
            case "VERIFIED" -> "Verified";
            case "PARTIALLY_FULFILLED" -> "Partially Fulfilled";
            case "FULFILLED" -> "Fulfilled";
            case "REJECTED" -> "Rejected";
            default -> status.replace('_', ' ');
        };
    }

    /**
     * Page event helper that renders a uniform, professional footer on every page.
     */
    private static class FooterPageEvent extends PdfPageEventHelper {
        private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, TEXT_MUTED);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String footerText = "Disaster Relief Coordination System  •  Official Command Audit  •  Page " + writer.getPageNumber();

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(footerText, footerFont),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 14, 0);
        }
    }
}
