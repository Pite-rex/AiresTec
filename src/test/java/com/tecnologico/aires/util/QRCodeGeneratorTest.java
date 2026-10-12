package com.tecnologico.aires.util;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeGeneratorTest {

    @Test
    void generaUnPngConLasDimensionesSolicitadas() throws IOException, WriterException {
        byte[] png = QRCodeGenerator.generateQRCodeImage("aires-test", 180, 120);

        assertTrue(png.length > 0);
        assertEquals((byte) 0x89, png[0]);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image);
        assertEquals(180, image.getWidth());
        assertEquals(120, image.getHeight());
    }

    @Test
    void rechazaTextoVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> QRCodeGenerator.generateQRCodeImage("", 120, 120));
    }
}
