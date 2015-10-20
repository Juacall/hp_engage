package apps.hp.hp_engage;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by bernajua on 10/13/2015.
 */
public class AuraAdapter extends RecyclerView.Adapter<AuraAdapter.ViewHolder> {
    private AuraDetails[] myDataset;
    private View.OnClickListener listener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public SquareImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.aura_text);
            mImageView = (SquareImageView) v.findViewById(R.id.aura_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AuraAdapter(AuraDetails[] myDataset,View.OnClickListener listener) {
        this.myDataset = myDataset;
        this.listener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AuraAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_items, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
         v.setOnClickListener(listener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.mTextView.setText(myDataset[position].aura.getName());
        holder.mImageView.setImageBitmap(myDataset[position].auraImage);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myDataset.length;
    }
}
