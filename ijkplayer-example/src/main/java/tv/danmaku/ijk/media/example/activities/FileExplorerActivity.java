/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.example.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import tv.danmaku.ijk.media.example.R;
import tv.danmaku.ijk.media.example.application.AppActivity;
import tv.danmaku.ijk.media.example.application.Settings;
import tv.danmaku.ijk.media.example.eventbus.FileExplorerEvents;
import tv.danmaku.ijk.media.example.fragments.FileListFragment;

public class FileExplorerActivity extends AppActivity {
    private Settings mSettings;
    private String localPath = "/storage/emulated/0/Movies/【迅雷下载www.dy131.com】黑客帝国1BD中英双字1280高清.rmvb";
    private String onlineDefaultPath = "http://video.dispatch.tc.qq.com/c0029dgfgok.p201.3.mp4?sdtfrom=v1001&type=mp4&vkey=504067CE4044E9FD6ABCC77731E948B76F19F12DCD8EEBD4E0B57F358EB2A2D24620E31179E175F441C850652410C3B721ED9003B0D94152BE43F0062A3971E8C42CD350B8E2FD9A1CA6346C841D645833C32A2E8D44CDD3419CB5D46605F9A169C4A51EDDEB7AF6707EB10B842D7C7E4E41F0F4F49BC33F&platform=11&fmt=auto&sp=350&guid=D1CF803B289530A0B7930136BE5A88D4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mSettings == null) {
            mSettings = new Settings(this);
        }

        String lastDirectory = mSettings.getLastDirectory();
        if (!TextUtils.isEmpty(lastDirectory) && new File(lastDirectory).isDirectory())
            doOpenDirectory(lastDirectory, false);
        else
            doOpenDirectory("/", false);

        initListener();
    }


    private void initListener() {
        btnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoActivity.intentTo(FileExplorerActivity.this, localPath, "");
            }
        });

        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String etUrl = etOnlineUrl.getText().toString().trim();

                VideoActivity.intentTo(FileExplorerActivity.this, TextUtils.isEmpty(etUrl) ? onlineDefaultPath : etUrl, "");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        FileExplorerEvents.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FileExplorerEvents.getBus().unregister(this);
    }

    private void doOpenDirectory(String path, boolean addToBackStack) {
        Fragment newFragment = FileListFragment.newInstance(path);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.body, newFragment);

        if (addToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    @Subscribe
    public void onClickFile(FileExplorerEvents.OnClickFile event) {
        File f = event.mFile;
        try {
            f = f.getAbsoluteFile();
            f = f.getCanonicalFile();
            if (TextUtils.isEmpty(f.toString()))
                f = new File("/");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (f.isDirectory()) {
            String path = f.toString();
            mSettings.setLastDirectory(path);
            doOpenDirectory(path, true);
        } else if (f.exists()) {
            VideoActivity.intentTo(this, f.getPath(), f.getName());
        }
    }
}
