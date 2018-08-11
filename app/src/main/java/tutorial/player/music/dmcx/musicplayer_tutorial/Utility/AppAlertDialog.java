package tutorial.player.music.dmcx.musicplayer_tutorial.Utility;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;

public class AppAlertDialog {

    private AlertDialog dialog;

    //
    public AppAlertDialog createDialog(
            Context ctx, boolean isCancelable, String title, String msg,
            String positiveBtnText, String negativeBtnText,
            AlertDialog.OnClickListener positiveClickListerner, AlertDialog.OnClickListener negativeClickListerner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(isCancelable);
        builder.setPositiveButton(positiveBtnText, positiveClickListerner);
        builder.setNegativeButton(negativeBtnText, negativeClickListerner);

        dialog = builder.create();
        return this;
    }

    // Basic Dialog
    public AppAlertDialog createDialog(Context ctx, View view, boolean isCancelable, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(isCancelable);
        builder.setView(view);

        if (!title.equals("")) {
            builder.setTitle(title);
        }

        dialog = builder.create();
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

}
