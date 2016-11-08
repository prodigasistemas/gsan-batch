package br.gov.batch.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Util {
	
	public static int getMes(Date date) {
		Calendar dataCalendar = GregorianCalendar.getInstance();
		dataCalendar.setTime(date);

		return (dataCalendar.get(Calendar.MONTH) + 1);
	}

	public static int getAno(Date date) {
		Calendar dataCalendar = GregorianCalendar.getInstance();
		dataCalendar.setTime(date);

		return dataCalendar.get(Calendar.YEAR);
	}

	public static Integer getAnoMesComoInteger(Date date) {
		int mes = getMes(date);
		String sMes = mes + "";
		if (sMes.length() == 1) {
			sMes = "0" + sMes;
		}
		int ano = getAno(date);

		return new Integer(ano + "" + sMes);
	}
	
	public static Date getData(String dia, String mes, String ano) {
        Date retorno = null;

        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");

        String dataCompleta = dia + "/" + mes + "/" + ano;

        try {
			retorno = formatoData.parse(dataCompleta);
		} catch (ParseException e) {
			e.printStackTrace();
		}

        return retorno;
    }
}
