
package com.festora_backend.ticket.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class TicketQrService {

    public String generateDataUri(String ticketNumber) {
        try {
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix matrix = writer.encode(
                    ticketNumber,
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    Map.of(
                            EncodeHintType.MARGIN,
                            1
                    )
            );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return "data:image/png;base64,"
                    + Base64.getEncoder()
                    .encodeToString(output.toByteArray());

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to generate ticket QR code",
                    exception
            );
        }
    }
}
