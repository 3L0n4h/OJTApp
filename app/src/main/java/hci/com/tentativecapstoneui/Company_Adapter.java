package hci.com.tentativecapstoneui;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import hci.com.tentativecapstoneui.model.CompanyR;


public class Company_Adapter extends RecyclerView.Adapter<Company_Adapter.ProductViewHolder>
        implements View.OnClickListener{
    Context mCtx;
    List<CompanyR> companyList;

    private OnItemClickListener mListener;

    @Override
    public void onClick(View v) {


    }

    public interface OnItemClickListener{ //recycler click listener
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listen){
        mListener = listen;
    }

    public Company_Adapter(Context mCtx, List<CompanyR> companytList) {
        this.mCtx = mCtx;
        this.companyList = companytList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.company_layout,
                parent, false);
        ProductViewHolder productViewHolder = new ProductViewHolder(view, mListener);
        return productViewHolder;
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        CompanyR company = companyList.get(position);

        holder.textViewTitle.setText(company.getCompanyName());
        holder.textViewShortDesc.setText(company.getCompanyAddres());
        holder.textviewRemark.setText("Remarks: "); //set text here
//        holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(product.getImage(), null));

    }



    @Override
    public int getItemCount() {
        return companyList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewTitle, textViewShortDesc, textViewRating, textviewRemark;

        public ProductViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewAddress);
            textviewRemark = itemView.findViewById(R.id.textViewRemark);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(int i=0; i<companyList.size();i++)
                    {
                        companyList.get(i).setSelected(false);
                    }
                    companyList.get(getAdapterPosition()).setSelected(true);
                    // itemView.setBackgroundColor(Color.BLUE);
                }
            });

        }
    }
}

