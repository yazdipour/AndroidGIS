package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.content.Context;
import android.content.DialogInterface;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class DialogHandler {

    public static SweetAlertDialog getDialog(Context context,
                                             Integer type,
                                             String title,
                                             boolean isCancelable,
                                             boolean showCancelButton,
                                             DialogInterface.OnDismissListener dismissListener) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context);
        sweetAlertDialog.changeAlertType(type == null ? SweetAlertDialog.PROGRESS_TYPE : type);
        sweetAlertDialog.setTitleText(title)
                .setCancelable(isCancelable);
        sweetAlertDialog.showCancelButton(showCancelButton);
        sweetAlertDialog.setOnDismissListener(dismissListener == null ?
                dialogInterface -> sweetAlertDialog.cancel() : dismissListener);
        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialog(Context context,
                                             Integer type,
                                             int titleStringId,
                                             boolean isCancelable,
                                             boolean showCancelButton,
                                             DialogInterface.OnDismissListener dismissListener) {
        return getDialog(context, type,
                context.getString(titleStringId),
                isCancelable, showCancelButton,
                dismissListener);
    }
}
