package ui.common;

import category.CategoryDTO;
import exception.InputException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class CategorySelector {

  private static final long PLACEHOLDER_ID = -1;
  private static final long ALL_ID = 0;

  private final JPanel controls;
  private final JButton search;
  private final boolean includeAllInChildBoxes;
  private final int fixedComponentCount;
  private final Map<Long, List<CategoryDTO>> childrenByParent = new LinkedHashMap<>();
  private final List<JComboBox<SelectOption>> boxes = new ArrayList<>();
  private boolean adjusting;

  public CategorySelector(JPanel controls, JButton search) {
    this(controls, search, true, 1);
  }

  public CategorySelector(JPanel controls) {
    this(controls, null, false, 0);
  }

  private CategorySelector(JPanel controls, JButton search, boolean includeAllInChildBoxes,
      int fixedComponentCount) {
    this.controls = controls;
    this.search = search;
    this.includeAllInChildBoxes = includeAllInChildBoxes;
    this.fixedComponentCount = fixedComponentCount;
  }

  public void load(List<CategoryDTO> categories) {
    childrenByParent.clear();
    boxes.clear();

    for (CategoryDTO category : categories) {
      childrenByParent
          .computeIfAbsent(category.getParentCategoryId(), key -> new ArrayList<>())
          .add(category);
    }
    addBox(null, false);
  }

  public Set<Long> selectedCategoryIds() {
    Long selectedId = selectedCategoryId();
    if (selectedId == null) {
      return java.util.Collections.emptySet();
    }

    Set<Long> ids = new HashSet<>();
    collectDescendantIds(selectedId, ids);
    return ids;
  }

  public long selectedLeafCategoryId(String label) {
    Long selectedId = selectedCategoryId();
    if (selectedId == null) {
      throw new InputException(label + "를 선택해 주세요.");
    }
    if (hasChildren(selectedId)) {
      throw new InputException("최하단 카테고리까지 선택해 주세요.");
    }
    return selectedId;
  }

  private void addBox(Long parentId, boolean includeAll) {
    List<CategoryDTO> children = childrenByParent.getOrDefault(parentId,
        java.util.Collections.emptyList());
    if (children.isEmpty()) {
      refreshControls();
      return;
    }

    JComboBox<SelectOption> box = new JComboBox<>();
    box.addItem(new SelectOption(PLACEHOLDER_ID, "선택"));
    if (includeAll) {
      box.addItem(new SelectOption(ALL_ID, "전체"));
    }
    for (CategoryDTO category : children) {
      box.addItem(new SelectOption(category.getCategoryId(), category.getCategoryName()));
    }
    box.addActionListener(event -> updateChildren(box));
    boxes.add(box);
    refreshControls();

    if (!includeAll) {
      updateChildren(box);
    }
  }

  private void updateChildren(JComboBox<SelectOption> changedBox) {
    if (adjusting) {
      return;
    }

    adjusting = true;
    try {
      int index = boxes.indexOf(changedBox);
      while (boxes.size() > index + 1) {
        boxes.remove(boxes.size() - 1);
      }

      SelectOption selected = (SelectOption) changedBox.getSelectedItem();
      if (selected != null && selected.getId() > 0 && hasChildren(selected.getId())) {
        addBox(selected.getId(), includeAllInChildBoxes);
      } else {
        refreshControls();
      }
    } finally {
      adjusting = false;
    }
  }

  private boolean hasChildren(long categoryId) {
    return !childrenByParent.getOrDefault(categoryId, java.util.Collections.emptyList()).isEmpty();
  }

  private Long selectedCategoryId() {
    Long selectedId = null;
    for (JComboBox<SelectOption> box : boxes) {
      SelectOption option = (SelectOption) box.getSelectedItem();
      if (option != null && option.getId() > 0) {
        selectedId = option.getId();
      } else if (option == null || option.getId() == PLACEHOLDER_ID) {
        break;
      }
    }
    return selectedId;
  }

  private void collectDescendantIds(Long categoryId, Set<Long> ids) {
    ids.add(categoryId);
    for (CategoryDTO child : childrenByParent.getOrDefault(categoryId,
        java.util.Collections.emptyList())) {
      collectDescendantIds(child.getCategoryId(), ids);
    }
  }

  private void refreshControls() {
    while (controls.getComponentCount() > fixedComponentCount) {
      controls.remove(fixedComponentCount);
    }
    for (JComboBox<SelectOption> box : boxes) {
      controls.add(box);
    }
    if (search != null) {
      controls.add(search);
    }
    controls.revalidate();
    controls.repaint();
  }
}
