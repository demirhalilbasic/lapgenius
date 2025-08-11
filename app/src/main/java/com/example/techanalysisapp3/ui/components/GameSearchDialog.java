package com.example.techanalysisapp3.ui.components;

import static com.example.techanalysisapp3.util.AppConstants.RAWG_API_KEY;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.techanalysisapp3.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GameSearchDialog extends DialogFragment {
    public interface OnGameSelectedListener {
        void onGameSelected(int gameId, String name, String imageUrl);
    }
    private OnGameSelectedListener listener;
    private EditText etSearch;
    private RecyclerView rv;
    private GameAdapter adapter;
    private OkHttpClient client = new OkHttpClient();
    private static final String[] EXPLICIT_TAGS = {
            "nudity", "sexual content", "nsfw", "adult", "porn", "porn-game",
            "hentai", "хентай", "erotic"
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch.postDelayed(() -> {
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment target = getTargetFragment();
        if (!(target instanceof OnGameSelectedListener)) {
            throw new RuntimeException("Parent must implement OnGameSelectedListener");
        }
        listener = (OnGameSelectedListener) target;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        View view = inflater.inflate(R.layout.dialog_game_search, container, false);
        etSearch = view.findViewById(R.id.etSearchGame);
        Button btn = view.findViewById(R.id.btnSearchGame);
        rv = view.findViewById(R.id.rvGames);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GameAdapter();
        rv.setAdapter(adapter);
        btn.setOnClickListener(x -> doSearch());

        etSearch.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                doSearch();
                return true;
            }
            return false;
        });

        return view;
    }

    private void doSearch() {
        String q = Uri.encode(etSearch.getText().toString());
        Request r = new Request.Builder()
                .url("https://api.rawg.io/api/games?key="+RAWG_API_KEY+"&search="+q)
                .build();
        client.newCall(r).enqueue(new Callback(){
            @Override public void onFailure(Call c, IOException e) {}
            @Override public void onResponse(Call c, Response res) throws IOException {
                try {
                    JSONArray arr = new JSONObject(res.body().string())
                            .getJSONArray("results");
                    List<Game> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);

                        String esrbSlug = "";
                        if (o.has("esrb_rating") && !o.isNull("esrb_rating")) {
                            JSONObject esrbRating = o.getJSONObject("esrb_rating");
                            esrbSlug = esrbRating.optString("slug", "");
                        }

                        if ("adults-only".equals(esrbSlug)) {
                            continue;
                        }

                        List<String> tagList = new ArrayList<>();
                        if (o.has("tags") && !o.isNull("tags")) {
                            JSONArray tags = o.getJSONArray("tags");
                            for (int j = 0; j < tags.length(); j++) {
                                JSONObject tag = tags.getJSONObject(j);
                                tagList.add(tag.getString("name").toLowerCase());
                            }
                        }

                        if (containsExplicitTag(tagList)) {
                            continue;
                        }

                        list.add(new Game(
                                o.getInt("id"),
                                o.getString("name"),
                                o.optString("background_image", "")
                        ));
                    }
                    getActivity().runOnUiThread(() -> adapter.setGames(list));
                } catch (Exception e) {}
            }
        });

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private boolean containsExplicitTag(List<String> tags) {
        for (String tag : tags) {
            for (String explicit : EXPLICIT_TAGS) {
                if (tag.contains(explicit)) {
                    return true;
                }
            }
        }
        return false;
    }

    class GameAdapter extends RecyclerView.Adapter<GameAdapter.VH> {
        private List<Game> games = new ArrayList<>();
        void setGames(List<Game> g){ games=g; notifyDataSetChanged(); }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int i){
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_game,p,false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h,int i){
            Game g=games.get(i);
            h.tv.setText(g.name);
            Glide.with(h.iv).load(g.image).into(h.iv);
            h.itemView.setOnClickListener(v->{
                listener.onGameSelected(g.id,g.name,g.image);
                dismiss();
            });
        }
        @Override public int getItemCount(){return games.size();}
        class VH extends RecyclerView.ViewHolder{
            ImageView iv; TextView tv;
            VH(View v){super(v);
                iv=v.findViewById(R.id.ivGame);
                tv=v.findViewById(R.id.tvGameName);
            }
        }
    }
    static class Game {int id;String name,image;Game(int i,String n,String im){id=i;name=n;image=im;}}
}