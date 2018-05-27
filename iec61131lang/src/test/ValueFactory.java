/*-
 * #%L
 * iec61131lang
 * %%
 * Copyright (C) 2016 Alexander Weigl
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a clone of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import edu.kit.iti.formal.automation.datatypes.*;
import edu.kit.iti.formal.automation.datatypes.values.*;
import edu.kit.iti.formal.automation.sfclang.Utils;
import edu.kit.iti.formal.automation.st.ast.Expression;
import org.antlr.v4.runtime.Token;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by weigl on 11.06.14.
 *
 * @author weigl
 * @version $Id: $Id
 */
public class ValueFactory {
    /**
     * <p>makeInt.</p>
     *
     * @param value a long.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<Int, BigInteger> makeInt(long value) {
        return new ScalarValue<>(AnyInt.INT, BigInteger.valueOf(value));
    }

    /**
     * <p>makeUInt.</p>
     *
     * @param value a long.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<UInt, BigInteger> makeUInt(long value) {
        return new ScalarValue<>(AnyInt.UINT, BigInteger.valueOf(value));
    }

    /**
     * <p>makeSInt.</p>
     *
     * @param value a int.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<SInt, BigInteger> makeSInt(int value) {
        return new ScalarValue<>(AnyInt.SINT, BigInteger.valueOf(value));
    }

    /**
     * <p>makeLInt.</p>
     *
     * @param value a int.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<DInt, BigInteger> makeLInt(int value) {
        return new ScalarValue<>(AnyInt.DINT,
                BigInteger.valueOf(value));
    }

    /**
     * <p>makeDInt.</p>
     *
     * @param value a int.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<LInt, BigInteger> makeDInt(int value) {
        return new ScalarValue<>(AnyInt.LINT,
                BigInteger.valueOf(value));
    }


    /**
     * <p>makeInt.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<Int, BigInteger> makeInt() {
        return new ScalarValue<>(AnyInt.INT, AnyInt.DEFAULT);
    }

    /**
     * <p>makeSInt.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<SInt, BigInteger> makeSInt() {
        return new ScalarValue<>(AnyInt.SINT, AnyInt.DEFAULT);
    }

    /**
     * <p>makeLInt.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<DInt, BigInteger> makeLInt() {
        return new ScalarValue<>(AnyInt.DINT, AnyInt.DEFAULT);
    }

    /**
     * <p>makeDInt.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<LInt, BigInteger> makeDInt() {
        return new ScalarValue<>(AnyInt.LINT, AnyInt.DEFAULT);
    }


    /**
     * <p>makeAnyBit.</p>
     *
     * @param dataType a T object.
     * @param <T>      a T object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static <T extends AnyBit> ScalarValue<T, Bits> makeAnyBit(T dataType) {
        return new ScalarValue<>(dataType, new Bits(dataType.getBitLength()));
    }

    /**
     * <p>makeBool.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.Bool, Bits> makeBool() {
        return makeAnyBit(AnyBit.BOOL);
    }

    /**
     * <p>makeWord.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.Word, Bits> makeWord() {
        return makeAnyBit(AnyBit.WORD);
    }

    /**
     * <p>makeDWord.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.DWord, Bits> makeDWord() {
        return makeAnyBit(AnyBit.DWORD);
    }

    /**
     * <p>makeLWord.</p>
     *
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.LWord, Bits> makeLWord() {
        return makeAnyBit(AnyBit.LWORD);
    }

    /**
     * <p>parseLiteral.</p>
     *
     * @param text a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.st.ast.Expression} object.
     */
    public static Expression parseLiteral(String text) {
        return null;
    }

    /**
     * <p>parseBitLiteral.</p>
     *
     * @param s a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends AnyBit, Bits> parseBitLiteral(Token s) {
        ScalarValue<? extends AnyBit, Bits> val = parseBitLiteral(s.getText());
        Utils.setPosition(val, s);
        return val;
    }


    /**
     * <p>parseBitLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends AnyBit, Bits> parseBitLiteral(String s) {
        String first = getPrefix(s);

        if ("TRUE".equalsIgnoreCase(s))
            return new ScalarValue<>(AnyBit.BOOL, new Bits(1l, AnyBit.BOOL.getBitLength()));
        if ("FALSE".equalsIgnoreCase(s))
            return new ScalarValue<>(AnyBit.BOOL, new Bits(0l, AnyBit.BOOL.getBitLength()));

        if (first != null) {
            long value = parseOrdinal(removePrefix(s));
            switch (first) {
                case "BYTE":
                    return new ScalarValue<>(AnyBit.BYTE, new Bits(value, AnyBit.BYTE.getBitLength()));
                case "LWORD":
                    return new ScalarValue<>(AnyBit.LWORD, new Bits(value, AnyBit.LWORD.getBitLength()));
                case "WORD":
                    return new ScalarValue<>(AnyBit.WORD, new Bits(value, AnyBit.WORD.getBitLength()));
                case "DWORD":
                    return new ScalarValue<>(AnyBit.DWORD, new Bits(value, AnyBit.DWORD.getBitLength()));
                case "BOOL":
                    return new ScalarValue<>(AnyBit.BOOL, new Bits(value, AnyBit.BOOL.getBitLength()));
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * <p>parseIntegerLiteral.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends AnyInt, BigInteger> parseIntegerLiteral(Token token) {
        ScalarValue<? extends AnyInt, BigInteger> val = parseIntegerLiteral(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>parseIntegerLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends AnyInt, BigInteger> parseIntegerLiteral(String s) {
        String first = getPrefix(s);

        int bits = 0;
        BigInteger value = BigInteger.valueOf(parseOrdinal(removePrefix(s)));

        if (first == null || "INT".equalsIgnoreCase(first)) {
            return new ScalarValue<>(AnyInt.INT, value);
        } else {
            switch (first) {
                case "2":
                case "8":
                case "16":
                    value = BigInteger.valueOf(parseOrdinal((s)));
                    return new ScalarValue<>(AnyInt.INT, value);
                case "SINT":
                    return new ScalarValue<>(AnyInt.SINT, value);
                case "DINT":
                    return new ScalarValue<>(AnyInt.DINT, value);
                case "LINT":
                    return new ScalarValue<>(AnyInt.LINT, value);
                case "USINT":
                    return new ScalarValue<>(AnyInt.USINT, value);
                case "UINT":
                    return new ScalarValue<>(AnyInt.UINT, value);
                case "UDINT":
                    return new ScalarValue<>(AnyInt.UDINT, value);
                case "ULINT":
                    return new ScalarValue<>(AnyInt.ULINT, value);
            }
        }
        throw new IllegalStateException();
    }


    /**
     * <p>makeEnumeratedValue.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<EnumerateType, String> makeEnumeratedValue(Token token) {
        ScalarValue<EnumerateType, String> val = makeEnumeratedValue(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>makeEnumeratedValue.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<EnumerateType, String> makeEnumeratedValue(String s) {
        EnumerateType et = new EnumerateType(getPrefix(s));
        return new ScalarValue<>(et, removePrefix(s));
    }

    /**
     * <p>parseStringLiteral.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends IECString, String> parseStringLiteral(Token token) {
        ScalarValue<? extends IECString, String> val = parseStringLiteral(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>parseStringLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends IECString, String> parseStringLiteral(String s) {
        String content = s.substring(1, s.length() - 1);
        if (s.startsWith("'"))
            return new ScalarValue<IECString.String, String>(IECString.STRING_8BIT, content);
        else
            return new ScalarValue<IECString.WString, String>(IECString.STRING_16BIT, content);
    }

    /**
     * <p>parseTimeLiteral.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<TimeType, TimeValue> parseTimeLiteral(Token token) {
        ScalarValue<TimeType, TimeValue> val = parseTimeLiteral(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>parseTimeLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<TimeType, TimeValue> parseTimeLiteral(String s) {
        TimeValue tv = new TimeValue();
        s = removePrefix(s).replace("_", "");
        Pattern extractNumbers = Pattern.compile("([.0-9]+)([hmsd]{1,2})");
        Matcher matcher = extractNumbers.matcher(s);


        while (matcher.find()) {
            String num = matcher.group(1);
            String mod = matcher.group(2);

            double val = Double.parseDouble(num);

            switch (mod) {
                case "d":
                    tv.setDays(val);
                    break;

                case "h":
                    tv.setHours(val);
                    break;

                case "m":
                    tv.setMinutes(val);
                    break;

                case "s":
                    tv.setSeconds(val);
                    break;

                case "ms":
                    tv.setMillieseconds(val);
                    break;
            }
        }

        return new ScalarValue<>(TimeType.TIME_TYPE, tv);
    }

    /**
     * <p>makeBool.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.Bool, Bits> makeBool(Token token) {
        ScalarValue<AnyBit.Bool, Bits> val = makeBool(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>makeBool.</p>
     *
     * @param text a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.Bool, Bits> makeBool(String text) {
        return new ScalarValue<AnyBit.Bool, Bits>(AnyBit.BOOL, new Bits(text.equals("TRUE") ? 1 : 0, 1));
    }

    /**
     * <p>makeBool.</p>
     *
     * @param val a boolean.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyBit.Bool, Boolean> makeBool(boolean val) {
        return new ScalarValue<AnyBit.Bool, Boolean>(AnyBit.BOOL, val);
    }

    /**
     * <p>parseRealLiteral.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends AnyReal, Double> parseRealLiteral(Token token) {
        ScalarValue<? extends AnyReal, Double> val = parseRealLiteral(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>parseRealLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<? extends AnyReal, Double> parseRealLiteral(String s) {
        s = removePrefix(s);
        double val = Double.parseDouble(s);
        return new ScalarValue<>(AnyReal.LREAL, val);
    }

    /**
     * <p>parseDateAndTimeLiteral.</p>
     *
     * @param s a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyDate.DateAndTime, DateAndTimeData> parseDateAndTimeLiteral(Token s) {
        ScalarValue<AnyDate.DateAndTime, DateAndTimeData> val = parseDateAndTimeLiteral(s.getText());
        Utils.setPosition(val, s);
        return val;
    }

    /**
     * <p>parseDateAndTimeLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyDate.DateAndTime, DateAndTimeData> parseDateAndTimeLiteral(String s) {
        s = removePrefix(s);

        String a = s.substring(0, "yyyy-mm-dd".length());
        String b = s.substring("yyyy-mm-dd-".length());

        DateData date = parseDateLiteral(a).getValue();
        TimeofDayData tod = parseTimeOfDayLiteral(b).getValue();
        DateAndTimeData val = new DateAndTimeData(date, tod);
        return new ScalarValue<>(AnyDate.DATE_AND_TIME, val);
    }

    /**
     * <p>parseDateLiteral.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyDate.Date, DateData> parseDateLiteral(Token token) {
        ScalarValue<AnyDate.Date, DateData> val = parseDateLiteral(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>parseDateLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyDate.Date, DateData> parseDateLiteral(String s) {
        Pattern pattern = Pattern.compile("(?<year>\\d\\d\\d\\d)-(?<month>\\d?\\d)-(?<day>\\d?\\d)");

        Matcher matcher = pattern.matcher(removePrefix(s));
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group("year"));
            int month = Integer.parseInt(matcher.group("month"));
            int day = Integer.parseInt(matcher.group("day"));
            DateData date = new DateData(year, month, day);
            return new ScalarValue<>(AnyDate.DATE, date);
        } else {
            throw new IllegalArgumentException("given string is not a time of day value");
        }
    }

    /**
     * <p>parseTimeOfDayLiteral.</p>
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyDate.TimeOfDay, TimeofDayData> parseTimeOfDayLiteral(Token token) {
        ScalarValue<AnyDate.TimeOfDay, TimeofDayData> val = parseTimeOfDayLiteral(token.getText());
        Utils.setPosition(val, token);
        return val;
    }

    /**
     * <p>parseTimeOfDayLiteral.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link edu.kit.iti.formal.automation.datatypes.values.ScalarValue} object.
     */
    public static ScalarValue<AnyDate.TimeOfDay, TimeofDayData> parseTimeOfDayLiteral(String s) {
        Pattern pattern = Pattern.compile(
                "(?<hour>\\d?\\d):(?<min>\\d?\\d)(:(?<sec>\\d?\\d))?(.(?<ms>\\d+))?");

        Matcher matcher = pattern.matcher(removePrefix(s));
        Function<String, Integer> parseInt = (String key) -> {
            String a = matcher.group(key);
            if (a == null)
                return 0;
            else
                return Integer.parseInt(a);
        };

        if (matcher.matches()) {
            int hour = parseInt.apply("hour");
            int min = parseInt.apply("min");
            int sec = parseInt.apply("sec");
            int ms = parseInt.apply("ms");
            TimeofDayData tod = new TimeofDayData(hour, min, sec, ms);
            return new ScalarValue<>(AnyDate.TIME_OF_DAY, tod);

        } else {
            throw new IllegalArgumentException("given string is not a time of day value");
        }
    }


}
