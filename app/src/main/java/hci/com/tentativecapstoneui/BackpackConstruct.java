package hci.com.tentativecapstoneui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BackpackConstruct extends RecyclerView.Adapter<BackpackConstruct.ViewHolder> {

    RecyclerView recyclerView;
    Context context;
    ArrayList<String> items;
    ArrayList<String> urls;
    String urlOpen;

    public BackpackConstruct(){

    }

    public void update(String name, String url) {
        items.add(name);
        urls.add(url);
        notifyDataSetChanged();
    }


    public BackpackConstruct(RecyclerView recyclerView, Context context, ArrayList<String> items, ArrayList<String> urls) {
        this.recyclerView = recyclerView;
        this.context = context;
        this.items = items;
        this.urls = urls;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.backpack_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.nameOfFile.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameOfFile;

        public ViewHolder(View itemView) {
            super(itemView);
            nameOfFile = itemView.findViewById(R.id.nameOfFile);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildLayoutPosition(view);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setType(Intent.ACTION_VIEW);
//                    if (!urls.get(position).startsWith("https://") && !urls.get(position).startsWith("http://")){
//                        urlOpen = "http://" + urls.get(position);
//                    }
                    urlOpen = urls.get(position);
                    intent.setData(Uri.parse(urlOpen));
                    context.startActivity(intent);
                }
            });
        }
    }
}
