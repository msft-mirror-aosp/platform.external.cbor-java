package co.nstant.in.cbor.decoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Special;
import co.nstant.in.cbor.model.SpecialType;
import co.nstant.in.cbor.model.UnsignedInteger;

public class ArrayDecoderTest {
    @Test(expected = CborException.class)
    public void shouldThrowOnIncompleteArray() throws CborException {
        byte[] bytes = new byte[] { (byte) 0x82, 0x01 };
        CborDecoder.decode(bytes);
    }

    @Test(expected = CborException.class)
    public void shouldThrowInIncompleteIndefiniteLengthArray() throws CborException {
        byte[] bytes = new byte[] { (byte) 0x9f, 0x01, 0x02 };
        CborDecoder.decode(bytes);
    }

    @Test
    public void shouldDecodeEmptyIndefiniteLengthArrayAsEmpty() throws CborException {
        // 0x9f = indefinite-length array start, 0xff = break
        byte[] bytes = new byte[] { (byte) 0x9f, (byte) 0xff };
        List<DataItem> decoded = CborDecoder.decode(bytes);
        Array array = (Array) decoded.get(0);
        assertTrue("BREAK must not be decoded as an array element", array.getDataItems().isEmpty());
    }

    @Test
    public void shouldNotIncludeBreakAsElementOfIndefiniteLengthArray() throws CborException {
        // 0x9f 0x01 0x02 0xff = indefinite-length array containing [1, 2]
        byte[] bytes = new byte[] { (byte) 0x9f, 0x01, 0x02, (byte) 0xff };
        List<DataItem> decoded = CborDecoder.decode(bytes);
        Array array = (Array) decoded.get(0);
        assertEquals(2, array.getDataItems().size());
        assertEquals(new UnsignedInteger(1), array.getDataItems().get(0));
        assertEquals(new UnsignedInteger(2), array.getDataItems().get(1));
        for (DataItem item : array.getDataItems()) {
            boolean isBreak = item instanceof Special && ((Special) item).getSpecialType() == SpecialType.BREAK;
            assertTrue("no element of a decoded array should be the BREAK marker", !isBreak);
        }
    }
}
