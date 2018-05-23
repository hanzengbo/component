package xiaoka.tv.yzbcomponent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import tv.xiaoka.router.annotation.annonation.Autowired;
import tv.xiaoka.router.annotation.annonation.RouteNode;

@RouteNode(path = "/main", desc = "首页")
public class MainActivity extends AppCompatActivity {

    @Autowired(name="bookName")
    String bookName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
