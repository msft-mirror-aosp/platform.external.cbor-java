package co.nstant.in.cbor.examples;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnsignedInteger;

/**
 * [1, [2, 3], [_ 4,5]] -> 0x83 01 82 02 03 9f 04 05 ff
 */
public class Example77Test {

    private static final List<DataItem> VALUE = new CborBuilder().addArray().add(1).addArray().add(2).add(3).end()
        .startArray().add(4).add(5).end().end().build();

    // Distinct from VALUE: ArrayBuilder#end() adds a trailing Special.BREAK data item to a
    // chunked array so that writeArray() in CborOutputStream, which has no other way to
    // terminate a chunked array, can encode it correctly. Decoding well-formed CBOR must not
    // reproduce that internal encoder convention: an indefinite-length array's logical value has
    // exactly as many elements as it was written with, so BREAK must never appear as a data item
    // in a decoded Array.
    private static final List<DataItem> DECODED_VALUE;
    static {
        Array inner = new Array().add(new UnsignedInteger(4)).add(new UnsignedInteger(5));
        inner.setChunked(true);
        Array outer = new Array().add(new UnsignedInteger(1))
            .add(new Array().add(new UnsignedInteger(2)).add(new UnsignedInteger(3)))
            .add(inner);
        DECODED_VALUE = Collections.<DataItem>singletonList(outer);
    }

    private static final byte[] ENCODED_VALUE = new byte[] { (byte) 0x83, 0x01, (byte) 0x82, 0x02, 0x03, (byte) 0x9f,
            0x04, 0x05, (byte) 0xff };

    @Test
    public void shouldEncode() {
        Assert.assertArrayEquals(ENCODED_VALUE, CborEncoder.encodeToBytes(VALUE));
    }

    @Test
    public void shouldDecode() throws CborException {
        InputStream inputStream = new ByteArrayInputStream(ENCODED_VALUE);
        CborDecoder decoder = new CborDecoder(inputStream);
        List<DataItem> dataItems = decoder.decode();
        Assert.assertArrayEquals(DECODED_VALUE.toArray(), dataItems.toArray());
    }

}
