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
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class ThreadedPrintDocumentAdapter extends
        PrintDocumentAdapter {
  abstract LayoutJob buildLayoutJob(PrintAttributes oldAttributes,
                                    PrintAttributes newAttributes,
                                    CancellationSignal cancellationSignal,
                                    LayoutResultCallback callback,
                                    Bundle extras);

  abstract WriteJob buildWriteJob(PageRange[] pages,
                                  ParcelFileDescriptor destination,
                                  CancellationSignal cancellationSignal,
                                  WriteResultCallback callback,
                                  Context ctxt);

  private Context ctxt=null;
  private String filename = "";
  private String foldernam = "";
  private ExecutorService threadPool= Executors.newFixedThreadPool(1);

  ThreadedPrintDocumentAdapter(Context ctxt, String filename, String foldername) {
    this.ctxt=ctxt;
    this.filename = filename;
    this.foldernam = foldername;
  }

  @Override
  public void onLayout(PrintAttributes oldAttributes,
                       PrintAttributes newAttributes,
                       CancellationSignal cancellationSignal,
                       LayoutResultCallback callback, Bundle extras) {
    threadPool.submit(buildLayoutJob(oldAttributes, newAttributes,
                                     cancellationSignal, callback,
                                     extras));
  }

  @Override
  public void onWrite(PageRange[] pages,
                      ParcelFileDescriptor destination,
                      CancellationSignal cancellationSignal,
                      WriteResultCallback callback) {
    threadPool.submit(buildWriteJob(pages, destination,
                                    cancellationSignal, callback, ctxt));
  }

  @Override
  public void onFinish() {
    threadPool.shutdown();
    deleteunnecessaryFile();
    super.onFinish();
  }

  private void deleteunnecessaryFile() {

    /*File file = null;
    try {
      String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + filename;
      file = new File(filepath);
      if (file.exists())
        file.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
*/

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

      }
      //File myDirectory = new File(rootDirtory, foldernam);
//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File file = new File(myDirectory, filename);
      if (file.exists())
        file.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  protected abstract static class LayoutJob implements Runnable {
    PrintAttributes oldAttributes;
    PrintAttributes newAttributes;
    CancellationSignal cancellationSignal;
    LayoutResultCallback callback;
    Bundle extras;

    LayoutJob(PrintAttributes oldAttributes,
              PrintAttributes newAttributes,
              CancellationSignal cancellationSignal,
              LayoutResultCallback callback, Bundle extras) {
      this.oldAttributes=oldAttributes;
      this.newAttributes=newAttributes;
      this.cancellationSignal=cancellationSignal;
      this.callback=callback;
      this.extras=extras;
    }
  }

  protected abstract static class WriteJob implements Runnable {
    PageRange[] pages;
    ParcelFileDescriptor destination;
    CancellationSignal cancellationSignal;
    WriteResultCallback callback;
    Context ctxt;

    WriteJob(PageRange[] pages, ParcelFileDescriptor destination,
             CancellationSignal cancellationSignal,
             WriteResultCallback callback, Context ctxt) {
      this.pages=pages;
      this.destination=destination;
      this.cancellationSignal=cancellationSignal;
      this.callback=callback;
      this.ctxt=ctxt;
    }
  }
}
