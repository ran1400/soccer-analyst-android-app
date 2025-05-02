package ran.tmpTest.utils;

import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ExelHandel
{
    private Game game;
    private Cell cell;
    private Sheet sheet;
    private Workbook workbook = new HSSFWorkbook();
    private CellStyle headerCellStyle;

    public ExelHandel(Game game)
    {
        this.game = game;
    }
    public boolean makeEventsFile()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.getDefault());
        String time = sdf.format(new Date());
        String fixedGameName = game.gameName.replaceAll("[\\\\/:*?\"<>|]", "-");
        String fileName = fixedGameName + " " + time + ".xls";
        return makeEventsFileHelper(fileName);
    }

    private boolean makeEventsFileHelper(String fileName)
    {
        boolean isWorkbookWrittenIntoStorage;

        // Check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.d("exelHandel", "Storage not available or read only");
            return false;
        }

        // Creating a New HSSF Workbook (.xls format)
        workbook = new HSSFWorkbook();

        setHeaderCellStyle();
        sheet = workbook.createSheet(fileName);
        sheet.setColumnWidth(0, (7 * 400));
        sheet.setColumnWidth(1, (4 * 400));
        sheet.setColumnWidth(2, (7 * 400));
        sheet.setColumnWidth(3, (6 * 400));
        sheet.setColumnWidth(4, (15 * 400));


        setHeaders();
        fillData();
        isWorkbookWrittenIntoStorage = storeExcelInStorage(fileName);

        return isWorkbookWrittenIntoStorage;
    }

    private void fillData()
    {
        String gamePart,team,playerNum;
        Row row;
        int rowNum = 1;
        List<Event> events = game.events;
        for (Event event : events)
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
        String headers[] = {"game part","clock","team","player num","event"};
        for (int i = 0 ; i < headers.length ; i++)
        {
            cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
    }

    private void setHeaderCellStyle()
    {
        headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(HSSFColor.AQUA.index);
        headerCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    }

    private boolean storeExcelInStorage(String fileName)
    {
        boolean isSuccess;
        File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadPath ,fileName);
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            Log.d("exelHandel", "Writing file" + file);
            isSuccess = true;
        }
        catch (IOException e)
        {
            Log.d("exelHandel", "Error writing Exception: ", e);
            isSuccess = false;
        }
        catch (Exception e)
        {
            Log.d("exelHandel", "Failed to save file due to Exception: ", e);
            isSuccess = false;
        }
        finally
        {
            try
            {
                if (null != fileOutputStream)
                    fileOutputStream.close();

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return isSuccess;
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
