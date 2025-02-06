package com.example.ptfAttendanceSystem.qr;



import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class QRCodeService {
    private static final int QR_CODE_WIDTH = 250;
    private static final int QR_CODE_HEIGHT = 250;

    private String currentQRCode;
    private int statusFlag = 0;

    public String generateQRCodeAsString(String qrContent) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] qrImageData = outputStream.toByteArray();

        return Base64.getEncoder().encodeToString(qrImageData);
    }

    private String generateCurrentQRCode() throws WriterException, IOException {
        LocalDateTime now = LocalDateTime.now();
        String qrContent = "date:" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                " time:" + now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        return generateQRCodeAsString(qrContent);
    }

    @PostConstruct
    public void generateInitialQRCode() {
        regenerateQRCode();
    }

    public void regenerateQRCode() {
        try {
            this.currentQRCode = generateCurrentQRCode();
            this.statusFlag = 0;
            System.out.println("QR Code Regenerated: " + this.currentQRCode);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentQRCode() {
        return this.currentQRCode;
    }

    public int getStatusFlag() {
        return this.statusFlag;
    }

    public void setStatusFlag(int statusFlag) {
        this.statusFlag = statusFlag;
    }
}