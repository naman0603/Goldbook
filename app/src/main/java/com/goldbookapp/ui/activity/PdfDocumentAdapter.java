/***
  Copyright (c) 2014 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _The Busy Coder's Guide to Android Development_
    https://commonsware.com/Android
 */

package com.goldbookapp.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;
import android.widget.Switch;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class  PdfDocumentAdapter extends ThreadedPrintDocumentAdapter {
  static String filenam = "";
  static String foldernam = "";
  public PdfDocumentAdapter(Context ctxt,String filename, String foldername) {
    super(ctxt,filename,foldername);
    filenam = filename;
    foldernam = foldername;
  }

  @Override
  LayoutJob buildLayoutJob(PrintAttributes oldAttributes,
                           PrintAttributes newAttributes,
                           CancellationSignal cancellationSignal,
                           PrintDocumentAdapter.LayoutResultCallback callback, Bundle extras) {
    return(new PdfLayoutJob(oldAttributes, newAttributes,
                            cancellationSignal, callback, extras));
  }

  @Override
  WriteJob buildWriteJob(PageRange[] pages,
                         ParcelFileDescriptor destination,
                         CancellationSignal cancellationSignal,
                         PrintDocumentAdapter.WriteResultCallback callback, Context ctxt) {
    return(new PdfWriteJob(pages, destination, cancellationSignal,
                           callback, ctxt));
  }

  private static class PdfLayoutJob extends LayoutJob {
    PdfLayoutJob(PrintAttributes oldAttributes,
                 PrintAttributes newAttributes,
                 CancellationSignal cancellationSignal,
                 PrintDocumentAdapter.LayoutResultCallback callback, Bundle extras) {
      super(oldAttributes, newAttributes, cancellationSignal, callback,
            extras);
    }

    @Override
    public void run() {
      if (cancellationSignal.isCanceled()) {
        callback.onLayoutCancelled();
      }
      else {
        PrintDocumentInfo.Builder builder=
            new PrintDocumentInfo.Builder(filenam);

        builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
               .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
               .build();

        callback.onLayoutFinished(builder.build(),
                                  !newAttributes.equals(oldAttributes));
      }
    }
  }

  private class PdfWriteJob extends WriteJob {
    PdfWriteJob(PageRange[] pages, ParcelFileDescriptor destination,
                CancellationSignal cancellationSignal,
                PrintDocumentAdapter.WriteResultCallback callback, Context ctxt) {
      super(pages, destination, cancellationSignal, callback, ctxt);
    }

    @Override
    public void run() {
      InputStream in=null;
      OutputStream out=null;

      try {
        File rootDirtory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "GoldBook");
        File myDirectory = new File(rootDirtory, foldernam);
        switch (foldernam) {

          case "Contact":{
           rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Contact");
            break;
          }
          case "Stock":{
            rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Stock");
            break;
          }
          case "Invoice":{
            rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Invoice");
            break;
          }
          case "Receipt":{
            rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Receipt");
            break;
          }
          case "Sales_Voucher":{
           // rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Sales");
            break;
          }
          case "Purchase_Voucher":{
           // rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Purchase");
            break;
          }
          case "Payment_Voucher":{
            // rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Payment");
            break;
          }
          case "Receipt_Voucher":{
            // rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Receipt");
            break;
          }

          case "Day":{
            rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Day");
            break;
          }
          case "Cashbank":{
            rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Cashbank");
            break;
          }
          case "Ledger":{
            rootDirtory = new File(rootDirtory, "Reports");
            myDirectory = new File(rootDirtory, "Ledger");
            break;
          }

        }
        //File myDirectory = new File(rootDirtory, foldernam);
//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(myDirectory, filenam);

       in = FileUtils.openInputStream(file);
       /* in=ctxt.getAssets().open("cover.pdf");*/
        out=new FileOutputStream(destination.getFileDescriptor());

        byte[] buf=new byte[16384];
        int size;

        while ((size=in.read(buf)) >= 0
            && !cancellationSignal.isCanceled()) {
          out.write(buf, 0, size);
        }


        if (cancellationSignal.isCanceled()) {
          callback.onWriteCancelled();
        }
        else {
          callback.onWriteFinished(new PageRange[] { PageRange.ALL_PAGES });
        }
      }
      catch (Exception e) {
        callback.onWriteFailed(e.getMessage());
        Log.e(getClass().getSimpleName(), "Exception printing PDF", e);
      }
      finally {
        try {
          in.close();
          out.close();
        }
        catch (IOException e) {
          Log.e(getClass().getSimpleName(),
                "Exception cleaning up from printing PDF", e);
        }
      }
    }
  }
}
