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
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;

/**
 * {_ "a": 1, "b": [_ 2, 3]} -> 0xbf61610161629f0203ffff
 */
public class Example80Test {

    private static final List<DataItem> VALUE = new CborBuilder().startMap().put("a", 1).put("b", 2).startArray("b")
        .add(2).add(3).end().end().build();

    // See Example77Test for why this differs from VALUE: ArrayBuilder#end() deliberately leaves
    // a trailing Special.BREAK data item in a chunked array, needed by writeArray() in
    // CborOutputStream to terminate the encoding, but a correctly decoded Array must not contain
    // it. MapBuilder/MapDecoder don't have this issue, so only the nested array differs from
    // VALUE.
    private static final List<DataItem> DECODED_VALUE;
    static {
        Array nestedArray = new Array().add(new UnsignedInteger(2)).add(new UnsignedInteger(3));
        nestedArray.setChunked(true);
        Map map = new Map();
        map.setChunked(true);
        map.put(new UnicodeString("a"), new UnsignedInteger(1));
        map.put(new UnicodeString("b"), nestedArray);
        DECODED_VALUE = Collections.<DataItem>singletonList(map);
    }

    private static final byte[] ENCODED_VALUE = new byte[] { (byte) 0xbf, 0x61, 0x61, 0x01, 0x61, 0x62, (byte) 0x9f,
            0x02, 0x03, (byte) 0xff, (byte) 0xff };

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
