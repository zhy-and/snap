package com.snap.intelligence.musics.ui.fragments.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zionhuang.innertube.MainBe;
import com.zionhuang.innertube.models.AlbumItem;
import com.zionhuang.innertube.models.ArtistItem;
import com.zionhuang.innertube.models.PlaylistItem;
import com.zionhuang.innertube.models.SongItem;
import com.zionhuang.innertube.models.YTItem;
import com.zionhuang.innertube.pages.SearchSummary;
import com.snap.intelligence.musics.R;
import com.snap.intelligence.musics.playback.PlayerConnection;

import java.util.ArrayList;
import java.util.List;

public class OnlineSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SONG = 1;
    private static final int VIEW_TYPE_ALBUM = 2;
    private static final int VIEW_TYPE_ARTIST = 3;
    private static final int VIEW_TYPE_PLAYLIST = 4;

    private final List<Object> items = new ArrayList<>();
    private final PlayerConnection playerConnection;
    private final OnItemClickListener listener;

    public OnlineSearchAdapter(PlayerConnection playerConnection, OnItemClickListener listener) {
        this.playerConnection = playerConnection;
        this.listener = listener;
    }

    public void submitList(List<SearchSummary> summaries, MainBe.SearchFilter currentFilter) {
        items.clear();

        if (currentFilter == null) {
            // 显示所有类型的结果，每种类型前添加一个标题
            for (SearchSummary summary : summaries) {
                if (!summary.getItems().isEmpty()) {
                    items.add(summary.getTitle()); // 添加标题
                    items.addAll(summary.getItems()); // 添加该类型的所有项目
                }
            }
        } else {
            // 只显示当前过滤器类型的结果
            for (SearchSummary summary : summaries) {
                if (!summary.getItems().isEmpty()) {
                    // 根据过滤器类型筛选项目
                    boolean shouldAdd = false;
//                    for (YTItem item : summary.getItems()) {
//                        if ((currentFilter.toString() == YouTube.SearchFilter.getFILTER_SONG() && item instanceof SongItem) ||
//                            (currentFilter.toString() == YouTube.SearchFilter.getFILTER_VIDEO() && item instanceof SongItem) ||
//                            (currentFilter.toString() == YouTube.SearchFilter.getFILTER_ALBUM() && item instanceof AlbumItem) ||
//                            (currentFilter .toString()== YouTube.SearchFilter.getFILTER_ARTIST() && item instanceof ArtistItem) ||
//                            ((currentFilter.toString() == YouTube.SearchFilter.getFILTER_COMMUNITY_PLAYLIST() ||
//                              currentFilter.toString() == YouTube.SearchFilter.getFILTER_FEATURED_PLAYLIST()) &&
//                             item instanceof PlaylistItem)) {
//                            shouldAdd = true;
//                            break;
//                        }
//                    }
                    if (shouldAdd) {
                        items.addAll(summary.getItems());
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    public void submitItems(List<YTItem> ytItems) {
        items.clear();
        items.addAll(ytItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof SongItem) {
            return VIEW_TYPE_SONG;
        } else if (item instanceof AlbumItem) {
            return VIEW_TYPE_ALBUM;
        } else if (item instanceof ArtistItem) {
            return VIEW_TYPE_ARTIST;
        } else if (item instanceof PlaylistItem) {
            return VIEW_TYPE_PLAYLIST;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_HEADER:
//                return new HeaderViewHolder(inflater.inflate(R.layout.item_section_header, parent, false));
            case VIEW_TYPE_SONG:
                return new SongViewHolder(inflater.inflate(R.layout.item_on_song, parent, false));
            case VIEW_TYPE_ALBUM:
//                return new AlbumViewHolder(inflater.inflate(R.layout.item_album, parent, false));
            case VIEW_TYPE_ARTIST:
//                return new ArtistViewHolder(inflater.inflate(R.layout.item_artist, parent, false));
            case VIEW_TYPE_PLAYLIST:
//                return new PlaylistViewHolder(inflater.inflate(R.layout.item_playlist, parent, false));
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                ((HeaderViewHolder) holder).bind((String) item);
                break;
            case VIEW_TYPE_SONG:
                ((SongViewHolder) holder).bind((SongItem) item);
                break;
            case VIEW_TYPE_ALBUM:
                ((AlbumViewHolder) holder).bind((AlbumItem) item);
                break;
            case VIEW_TYPE_ARTIST:
                ((ArtistViewHolder) holder).bind((ArtistItem) item);
                break;
            case VIEW_TYPE_PLAYLIST:
                ((PlaylistViewHolder) holder).bind((PlaylistItem) item);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 标题ViewHolder
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
        }

        public void bind(String title) {
            titleTextView.setText(title);
        }
    }

    // 歌曲ViewHolder
    class SongViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView;
        private final TextView subtitleView;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail);
            titleView = itemView.findViewById(R.id.title);
            subtitleView = itemView.findViewById(R.id.subtitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    SongItem song = (SongItem) items.get(position);
                    listener.onSongClick(song);
                }
            });

//            itemView.findViewById(R.id.menu_button).setOnClickListener(v -> {
//                int position = getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    SongItem song = (SongItem) items.get(position);
//                    listener.onSongMenuClick(song, v);
//                }
//            });
        }

        public void bind(SongItem song) {
            titleView.setText(song.getTitle());
            
            // 构建艺术家名称
            StringBuilder artistsText = new StringBuilder();
            for (int i = 0; i < song.getArtists().size(); i++) {
                if (i > 0) artistsText.append(", ");
                artistsText.append(song.getArtists().get(i).getName());
            }
            subtitleView.setText(artistsText.toString());
            
            // 加载歌曲封面
            Glide.with(thumbnailView)
                    .load(song.getThumbnail())
                    .placeholder(R.drawable.music_note)
                    .into(thumbnailView);
            
            // 设置当前播放状态
            boolean isActive = playerConnection != null && 
                    playerConnection.getMediaMetadata() != null && 
                    song.getId().equals(playerConnection.getMediaMetadata().getValue().getId());
            
            if (isActive) {
//                titleView.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary, null));
            } else {
//                titleView.setTextColor(itemView.getContext().getResources().getColor(R.color.colorOnSurface, null));
            }
        }
    }

    // 专辑ViewHolder
    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView;
        private final TextView subtitleView;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail);
            titleView = itemView.findViewById(R.id.title);
            subtitleView = itemView.findViewById(R.id.subtitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    AlbumItem album = (AlbumItem) items.get(position);
                    listener.onAlbumClick(album);
                }
            });
        }

        public void bind(AlbumItem album) {
            titleView.setText(album.getTitle());
            
            // 构建艺术家名称
            if (album.getArtists() != null) {
                StringBuilder artistsText = new StringBuilder();
                for (int i = 0; i < album.getArtists().size(); i++) {
                    if (i > 0) artistsText.append(", ");
                    artistsText.append(album.getArtists().get(i).getName());
                }
                subtitleView.setText(artistsText.toString());
            } else {
                subtitleView.setText("");
            }
            
            // 加载专辑封面
            Glide.with(thumbnailView)
                    .load(album.getThumbnail())
                    .placeholder(R.drawable.music_note)
                    .into(thumbnailView);
        }
    }

    // 艺术家ViewHolder
    class ArtistViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail);
            titleView = itemView.findViewById(R.id.title);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ArtistItem artist = (ArtistItem) items.get(position);
                    listener.onArtistClick(artist);
                }
            });
        }

        public void bind(ArtistItem artist) {
            titleView.setText(artist.getTitle());
            
            // 加载艺术家图片
            Glide.with(thumbnailView)
                    .load(artist.getThumbnail())
                    .placeholder(R.drawable.person)
                    .into(thumbnailView);
        }
    }

    // 播放列表ViewHolder
    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView;
        private final TextView subtitleView;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail);
            titleView = itemView.findViewById(R.id.title);
            subtitleView = itemView.findViewById(R.id.subtitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PlaylistItem playlist = (PlaylistItem) items.get(position);
                    listener.onPlaylistClick(playlist);
                }
            });
        }

        public void bind(PlaylistItem playlist) {
            titleView.setText(playlist.getTitle());
            if (playlist.getAuthor() != null) {
                subtitleView.setText(playlist.getAuthor().getName());
            } else {
                subtitleView.setText("");
            }
            
            // 加载播放列表图片
            Glide.with(thumbnailView)
                    .load(playlist.getThumbnail())
                    .placeholder(R.drawable.media3_icon_album)
                    .into(thumbnailView);
        }
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onFilterClick(MainBe.SearchFilter filter);
        void onSongClick(SongItem song);
        void onSongMenuClick(SongItem song, View view);
        void onAlbumClick(AlbumItem album);
        void onArtistClick(ArtistItem artist);
        void onPlaylistClick(PlaylistItem playlist);
    }
}