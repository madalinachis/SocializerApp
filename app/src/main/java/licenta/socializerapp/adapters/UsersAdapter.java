package licenta.socializerapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import licenta.socializerapp.R;
import licenta.socializerapp.model.PeersUser;
import licenta.socializerapp.model.User;

/**
 * Created by Madalina on 5/10/2016.
 */
public class UsersAdapter extends ArrayAdapter<PeersUser> {

    @Bind(R.id.username_view)
    TextView usernameText;

    public UsersAdapter(Context context, List<PeersUser> users) {
        super(context, R.layout.anywall_post_item, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.anywall_post_item, parent, false);
        }
        ButterKnife.bind(this, convertView);
        PeersUser user = getItem(position);
        if (user != null) {
            usernameText.setText("User:" + user.getUser().getUsername());
        }
        return convertView;
    }
}
