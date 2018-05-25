package tv.xiaoka.liveroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tv.xiaoka.router.annotation.annonation.Autowired;
import tv.xiaoka.router.annotation.annonation.RouteNode;

@RouteNode(path = "/liveroom", desc = "直播间")
public class LiveRoomActivity extends AppCompatActivity {

    @Autowired(name="scid")
    public String mScid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
    }
}
