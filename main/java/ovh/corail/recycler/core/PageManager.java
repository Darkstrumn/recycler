package ovh.corail.recycler.core;

import java.util.List;

import com.google.common.collect.Lists;

public class PageManager {
	private List<List<RecyclingRecipe>> pages;
	private int pageNum;
	private String search = "";
	private int itemByPage = 4;
	
	public PageManager() {
		updatePages();
	}
	
	public void setPage(int pageNum) {
		this.pageNum = pageNum;
	}
	
	private List<RecyclingRecipe> getSubList(List<RecyclingRecipe> listIn, String match) {
		List<RecyclingRecipe> listOut = Lists.newArrayList();
		match = match.toLowerCase();
		for (RecyclingRecipe recipe : listIn) {
			if (recipe.getItemRecipe().getDisplayName().toLowerCase().contains(match)) {
				listOut.add(recipe);
			}
		}
		return listOut;
	}
	
	public void setSearch(String text) {
		search = text;
		updatePages();
	}
	
	public void updatePages() {
		pageNum=0;
		List<RecyclingRecipe> list = Lists.newArrayList();
		if (search.isEmpty()) {
			list = RecyclingManager.getInstance().recipes;
		} else {
			list = getSubList(RecyclingManager.getInstance().recipes, search);
		}
		pages = Lists.newArrayList();
		int pageCount = (int) Math.ceil(list.size()/(double)itemByPage);
		int currentPage = 0;
		int startingId, endingId;
		for (int i = 0 ; i < pageCount ; i++) {
			startingId = currentPage*itemByPage;
			endingId = startingId + itemByPage < list.size() ? startingId + itemByPage : list.size();
			pages.add(currentPage, list.subList(startingId, endingId));
			currentPage++;
		}
		if (pages.size() == 0) {
			pages.add(0, list);
		}
	}
	
	public int getPageCount() {
		return pages.size();
	}
	
	public List<RecyclingRecipe> getPage(int pageNum) {
		if (pageNum >= 0 && pageNum<pages.size()) {
			return pages.get(pageNum);
		}
		return null;
	}
	
	public int getPageNum() {
		return pageNum;
	}

}
