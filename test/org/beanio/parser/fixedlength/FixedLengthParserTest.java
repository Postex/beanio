/*
 * Copyright 2010-2011 Kevin Seim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beanio.parser.fixedlength;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.*;
import java.util.Map;

import org.beanio.*;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * JUnit test cases for fixed length streams.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthParserTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("fixedlength.xml");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testFieldDefinitions() throws Exception {
        BeanReader in = factory.createReader("f1", new InputStreamReader(
            getClass().getResourceAsStream("f1_valid.txt")));
        try {
            Map map;
            map = (Map) in.read();
            assertEquals(" value", map.get("default"));
            assertEquals(12345, map.get("number"));
            assertEquals("value", map.get("padx"));
            assertEquals("value", map.get("pos40"));

            StringWriter text = new StringWriter();
            BeanWriter out = factory.createWriter("f1", text);
            out.write(map);
            out.flush();
            out.close();
            assertEquals(" value    0000012345valuexxxxx          value", text.toString());
        }
        finally {
            in.close();
        }
    }

    @Test(expected = InvalidRecordException.class)
    public void testDefaultMinLengthValidation() {
        BeanReader in = factory.createReader("f1", new InputStreamReader(
            getClass().getResourceAsStream("f1_minLength.txt")));
        try {
            in.read();
        }
        finally {
            in.close();
        }
    }

    @Test(expected = InvalidRecordException.class)
    public void testDefaultMaxLengthValidation() {
        BeanReader in = factory.createReader("f1", new InputStreamReader(
            getClass().getResourceAsStream("f1_maxLength.txt")));
        try {
            in.read();
        }
        finally {
            in.close();
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOptionalField() {
        BeanReader in = factory.createReader("f2", new InputStreamReader(
            getClass().getResourceAsStream("f2_valid.txt")));
        try {
            Map map = (Map) in.read();
            assertEquals("value", map.get("field3"));

            map = (Map) in.read();
            assertEquals("value", map.get("field3"));

            map = (Map) in.read();
            assertNull(map.get("field3"));

            StringWriter text = new StringWriter();
            BeanWriter out = factory.createWriter("f2", text);
            out.write(map);
            assertEquals("1234512345     ", text.toString());
        }
        finally {
            in.close();
        }
    }

    @Test
    public void testValidation() {
        BeanReader in = factory.createReader("f2", new InputStreamReader(
            getClass().getResourceAsStream("f2_invalid.txt")));
        try {
            assertRecordError(in, 1, "record", "minLength, 1, Record Label, 12345, 10, 20");
            assertRecordError(in, 2, "record",
                "maxLength, 2, Record Label, 123456789012345678901, 10, 20");
            assertFieldError(in, 3, "record", "field3", "val", "Expected field length of 5 characters");
        }
        finally {
            in.close();
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testReader() {
        BeanReader in = factory.createReader("f3", new InputStreamReader(
            getClass().getResourceAsStream("f3_valid.txt")));
        try {
            Map map = (Map) in.read();
            assertEquals("00001", map.get("field1"));
            assertEquals("", map.get("field2"));
            assertEquals("XXXXX", map.get("field3"));
            
            StringWriter text = new StringWriter();
            factory.createWriter("f3", text).write(map);
            assertEquals("00001     XXXXX" + lineSeparator, text.toString());
            
            map = (Map) in.read();
            assertEquals("00002", map.get("field1"));
            assertEquals("Val2", map.get("field2"));
            
            map.put("field2", "Value2");
            
            text = new StringWriter();
            factory.createWriter("f3", text).write(map);
            assertEquals("00002Value" + lineSeparator, text.toString());
            
            in.read();
        }
        finally {
            in.close();
        }
        
    }
}
