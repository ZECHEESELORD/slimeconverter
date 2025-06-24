package sh.harold;

import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConverterIntegrationTest {
    @Test
    void testMcaToSlimeConversion() throws Exception {
        File mca = new File("src/test/resources/test_region.mca");
        assertTrue(mca.exists(), "Test MCA file missing");
        File worldDir = mca.getParentFile();
        int dataVersion = DataVersionUtil.readDataVersion(new File(worldDir, "../level.dat"));
        List<ChunkData> chunks = AnvilReader.loadChunks(worldDir);
        assertFalse(chunks.isEmpty(), "No chunks loaded from test MCA");
        File slime = File.createTempFile("test_output", ".slime");
        SlimeWorldWriter.write(chunks, dataVersion, slime);
        assertTrue(slime.exists() && slime.length() > 0, "No .slime file written");
        assertTrue(SlimeVerifier.verify(slime), ".slime file failed verification");
        slime.delete();
    }

    @Test
    void testSlimeVerifierRejectsCorrupt() throws Exception {
        File tmp = File.createTempFile("corrupt", ".slime");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(new byte[32]);
        }
        assertFalse(SlimeVerifier.verify(tmp), "Verifier should reject corrupt file");
        tmp.delete();
    }

    @Test
    void testSlimeVerifierRejectsWrongMagic() throws Exception {
        File tmp = File.createTempFile("wrongmagic", ".slime");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(tmp))) {
            out.writeShort(0xDEAD); // wrong magic
            out.writeByte(0x0C);
            out.writeInt(3216);
            out.writeInt(1);
            out.writeInt(1);
            out.writeByte(0); // fake chunk data
            out.writeInt(0);
            out.writeInt(0);
        }
        assertFalse(SlimeVerifier.verify(tmp), "Verifier should reject wrong magic");
        tmp.delete();
    }
}
