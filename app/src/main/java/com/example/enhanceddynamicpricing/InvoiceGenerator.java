package com.example.enhanceddynamicpricing;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceGenerator {

    private static final String TAG = "InvoiceGenerator";
    private static final double GST_RATE = 0.12; // 12% GST

    public static void generateInvoice(Context context, Booking booking) {
        try {
            // Calculate amounts
            long days = 1;
            if (booking.getFromDate() != null && booking.getToDate() != null) {
                long diff = booking.getToDate().getTime() - booking.getFromDate().getTime();
                days = Math.max(1, diff / (1000 * 60 * 60 * 24)); // Convert milliseconds to days
            }
            double baseAmount = booking.getPricePerDay() * days;
            double gstAmount = baseAmount * GST_RATE;
            double totalAmount = baseAmount + gstAmount;

            // Format dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fromDate = booking.getFromDate() != null ? dateFormat.format(booking.getFromDate()) : "N/A";
            String toDate = booking.getToDate() != null ? dateFormat.format(booking.getToDate()) : "N/A";

            // Create PDF
            String fileName = "Invoice_" + booking.getVehicleName() + "_" + System.currentTimeMillis() + ".pdf";
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                outputStream = resolver.openOutputStream(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues));
            } else {
                // Use legacy storage for Android 9 and below
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(directory, fileName);
                outputStream = new FileOutputStream(file);
            }

            // Initialize PDF document
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            document.add(new Paragraph(new Text("Rentwheels Invoice"))
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            // Add customer details
            document.add(new Paragraph("Customer Name: " + (booking.getName() != null ? booking.getName() : "N/A")));
            document.add(new Paragraph("Mobile: " + (booking.getMobileNumber() != null ? booking.getMobileNumber() : "N/A")));
            document.add(new Paragraph("Address: " + (booking.getAddress() != null ? booking.getAddress() : "N/A")));
            document.add(new Paragraph("Invoice Date: " + dateFormat.format(new Date())));
            document.add(new Paragraph("\n"));

            // Add booking details table
            float[] columnWidths = {1, 3};
            Table table = new Table(columnWidths);
            table.addCell("Vehicle Name");
            table.addCell(booking.getVehicleName());
            table.addCell("Vehicle Type");
            table.addCell(booking.getVehicleType());
            table.addCell("From Date");
            table.addCell(fromDate);
            table.addCell("To Date");
            table.addCell(toDate);
            table.addCell("Timings");
            table.addCell(booking.getTimings() != null ? booking.getTimings() : "N/A");
            table.addCell("Price per Day");
            table.addCell("₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(booking.getPricePerDay()));
            table.addCell("Number of Days");
            table.addCell(String.valueOf(days));
            document.add(table);

            // Add amount details
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Amount Details").setBold());
            document.add(new Paragraph("Base Amount: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(baseAmount)));
            document.add(new Paragraph("GST (12%): ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(gstAmount)));
            document.add(new Paragraph("Total Amount: ₹" + NumberFormat.getNumberInstance(Locale.getDefault()).format(totalAmount)).setBold());

            // Add footer
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Thank you for choosing Rentwheels!")
                    .setTextAlignment(TextAlignment.CENTER));

            // Close the document
            document.close();
            outputStream.close();

            Toast.makeText(context, "Invoice downloaded to Downloads folder: " + fileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Error generating invoice: " + e.getMessage(), e);
            Toast.makeText(context, "Failed to generate invoice: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}