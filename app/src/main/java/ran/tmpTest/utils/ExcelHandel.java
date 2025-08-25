package ran.tmpTest.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.util.Collection;

import ran.tmpTest.sharedData.AppData;

public class ExcelHandel
{
    private long gameId;
    private String gameName;
    private Uri uri;
    private Context context;
    private Cell cell;
    private Sheet sheet;
    private Workbook workbook = new HSSFWorkbook();
    private CellStyle headerCellStyle;

    public ExcelHandel(long gameId, String gameName, Context context, Uri uri)
    {
        this.gameId = gameId;
        this.gameName = gameName;
        this.uri = uri;
        this.context = context;
    }
    public boolean makeEventsFile()
    {
        boolean isWorkbookWrittenIntoStorage;

        // Check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.d("exelHandel", "Storage not available or read only");
            return false;
        }


        workbook = new XSSFWorkbook();

        setHeaderCellStyle();
        sheet = workbook.createSheet(gameName);
        sheet.setColumnWidth(0, (7 * 400));
        sheet.setColumnWidth(1, (4 * 400));
        sheet.setColumnWidth(2, (7 * 400));
        sheet.setColumnWidth(3, (8 * 400));
        sheet.setColumnWidth(4, (15 * 400));

        setHeaders();
        fillData();
        isWorkbookWrittenIntoStorage = storeExcelInStorage();

        return isWorkbookWrittenIntoStorage;
    }

    private void fillData()
    {
        String gamePart,team,playerNum;
        Row row;
        int rowNum = 1;
        Collection<EventInGame> events = AppData.dbRepository.eventInGameRepository.getEventsInGameWithBlocking(gameId);
        for (EventInGame event : events)
        {
            switch (event.gamePart)
            {
                case HALF_1 -> gamePart = "half 1";
                case HALF_2 -> gamePart = "half 2";
                case EXTRA_TIME_1 -> gamePart = "extra time 1";
                default -> gamePart = "extra time 2"; // case EXTRA_TIME_2
            }
            switch (event.team)
            {
                case NON -> team = "";
                case HOME_TEAM -> team = "home team";
                default -> team = "away team"; // case AWAY_TEAM
            }
            if (event.playerNum == 0)
                playerNum = "";
            else
                playerNum = String.valueOf(event.playerNum);
            row = sheet.createRow(rowNum++);

            cell = row.createCell(0);
            cell.setCellValue(gamePart);
            cell = row.createCell(1);
            cell.setCellValue(event.time);
            cell = row.createCell(2);
            cell.setCellValue(team);
            cell = row.createCell(3);
            cell.setCellValue(playerNum);
            cell = row.createCell(4);
            cell.setCellValue(event.eventName);
        }
    }

    private void setHeaders()
    {
        Row row = sheet.createRow(0);
        String[] headers = {"game part","clock","team","player num","event"};
        for (int i = 0 ; i < headers.length ; i++)
        {
            cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void setHeaderCellStyle()
    {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        xssfCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        xssfCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle = xssfCellStyle;
    }

    private boolean storeExcelInStorage()
    {
        try
        {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            workbook.write(outputStream);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean isExternalStorageAvailable()
    {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }

    private boolean isExternalStorageReadOnly()
    {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }

}
