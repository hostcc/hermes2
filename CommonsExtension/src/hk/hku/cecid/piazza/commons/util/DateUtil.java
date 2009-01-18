package hk.hku.cecid.piazza.commons.util;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil
{
    private static final String UTC_REGEX_PATTERN = "-?(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d+))?(Z|([\\+-]\\d{2}:\\d{2}))?";
    private static final int UTC_CAPTURE_GROUP = 9;
    private static final Pattern UTC_PATTERNER = Pattern.compile("-?(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d+))?(Z|([\\+-]\\d{2}:\\d{2}))?");

    public DateUtil()
    {
    }

    public static Timestamp UTC2Timestamp(String dateTime)
        throws UtilitiesException
    {
        return new Timestamp(UTC2MS(dateTime));
    }

    public static Date UTC2Date(String dateTime)
        throws UtilitiesException
    {
        return new Date(UTC2MS(dateTime));
    }

    public static long UTC2MS(String dateTime)
        throws UtilitiesException
    {
        return UTC2Calendar(dateTime).getTimeInMillis();
    }

    public static Calendar UTC2Calendar(String dateTime)
        throws UtilitiesException
    {
        Matcher m = UTC_PATTERNER.matcher(dateTime);
        if(m.matches() && m.groupCount() == 9)
        {
            int parts[] = new int[8];
            int i;
            for(i = 0; i < 6; i++)
                parts[i] = Integer.parseInt(m.group(i + 1));

            String ms = m.group(i + 1);
            if(ms == null)
            {
                parts[i++] = 0;
            } else
            {
                if(ms.length() > 3)
                    ms = ms.substring(0, 3);
                parts[i++] = Integer.parseInt(ms);
            }
            String tzStr = m.group(i + 1);
            TimeZone tz = null;
            if(tzStr == null || tzStr.equals("Z"))
                tz = TimeZone.getTimeZone("UTC");
            else
                tz = TimeZone.getTimeZone("GMT" + tzStr);
            Calendar c = Calendar.getInstance();
            c.clear();
            c.setTimeZone(tz);
            c.set(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
            c.add(14, parts[6]);
            return c;
        } else
        {
            throw new UtilitiesException("Unable to convert datetime to UTC format:" + dateTime);
        }
    }

    public static String date2UTC(Date dateTime, TimeZone timeZone)
        throws UtilitiesException
    {
		try {
			DecimalFormat twoDigits;
			String utc;
			String sign;
			int hours;
			int minutes;
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			dateFormatter.setLenient(false);
			dateFormatter.setTimeZone(timeZone);
			twoDigits = new DecimalFormat("00");
			utc = dateFormatter.format(dateTime);
			int tzOffset = timeZone.getOffset(dateTime.getTime());
			sign = "+";
			if(tzOffset < 0)
			{
				sign = "-";
				tzOffset = -tzOffset;
			}
			hours = tzOffset / 0x36ee80;
			minutes = (tzOffset % 0x36ee80) / 60000;
			return (new StringBuffer(utc.length() + 7)).append(utc).append(sign).append(twoDigits.format(hours)).append(":").append(twoDigits.format(minutes)).toString();
		}
	    catch (Exception ex)
   		{
      		throw new hk.hku.cecid.piazza.commons.util.UtilitiesException(ex);
    	}
    }

    public static String calendar2UTC(Calendar dateTime)
        throws UtilitiesException
    {
        return date2UTC(dateTime.getTime(), dateTime.getTimeZone());
    }

    public static String getCurrentUTCDateTime()
    {
	    try
    	{
      		return date2UTC(new Date(), TimeZone.getDefault());
		}
		catch (UtilitiesException ex) {
    		return null;
		}
    }

    public static Timestamp GMT2Timestamp(String dateTime)
        throws UtilitiesException
    {
        return new Timestamp(GMT2Date(dateTime).getTime());
    }

    public static long GMT2MS(String dateTime)
        throws UtilitiesException
    {
        return GMT2Date(dateTime).getTime();
    }

    public static Calendar GMT2Calender(String dateTime)
        throws UtilitiesException
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(GMT2Date(dateTime).getTime());
        return cal;
    }

    public static Date GMT2Date(String dateTime)
        throws UtilitiesException
    {
        Date GMTdate = DataFormatter.getInstance().parseDate(dateTime, "EEE MMM dd HH:mm:ss zz yyyy", Locale.US);
        if(GMTdate == null)
            throw new UtilitiesException("Unable to convert datetime to GMT format:" + dateTime);
        else
            return GMTdate;
    }
}
