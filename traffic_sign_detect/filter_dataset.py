import os
import sys
from collections import defaultdict

def process_labels(path, keep_labels, new_labels):
    """
    Xử lý các file trong thư mục labels.
    - Xóa các dòng không thuộc nhãn cần giữ lại (keep_labels).
    - Thay thế nhãn giữ lại bằng nhãn mới (new_labels).
    - Xóa file nhãn và file ảnh tương ứng nếu không tìm thấy nhãn nào ở keep_labels trong file nhãn.
    - Thống kê số lượng ảnh theo từng nhãn.

    Args:
        path (str): Đường dẫn thư mục chứa `images` và `labels`.
        keep_labels (list of int): Danh sách nhãn cần giữ lại.
        new_labels (list of int): Danh sách nhãn mới tương ứng để ghi đè.
    """
    labels_path = os.path.join(path, "labels")
    images_path = os.path.join(path, "images")

    if not os.path.exists(labels_path):
        print(f"Thư mục 'labels' không tồn tại trong đường dẫn: {path}")
        return

    if len(keep_labels) != len(new_labels):
        print("Danh sách nhãn cần giữ lại và nhãn mới không khớp về số lượng.")
        return

    label_mapping = dict(zip(keep_labels, new_labels))
    label_counts = defaultdict(int)

    for file_name in os.listdir(labels_path):
        file_path = os.path.join(labels_path, file_name)

        if not os.path.isfile(file_path):
            continue

        with open(file_path, "r") as file:
            lines = file.readlines()

        new_lines = []
        has_valid_label = False
        for line in lines:
            parts = line.strip().split()
            if len(parts) > 0:
                label = int(parts[0])
                if label in label_mapping:
                    new_label = label_mapping[label]
                    parts[0] = str(new_label)
                    new_lines.append(" ".join(parts))
                    label_counts[new_label] += 1
                    has_valid_label = True

        if has_valid_label:
            with open(file_path, "w") as file:
                file.write("\n".join(new_lines))
            print(f"Đã xử lý: {file_name}")
        else:
            # Xóa file nhãn và ảnh tương ứng nếu không tìm thấy nhãn hợp lệ
            os.remove(file_path)
            image_file = os.path.join(images_path, os.path.splitext(file_name)[0] + ".jpg")
            if os.path.exists(image_file):
                os.remove(image_file)
            print(f"Đã xóa file nhãn và ảnh: {file_name}, {os.path.splitext(file_name)[0] + '.jpg'}")

    # Thống kê số lượng ảnh theo nhãn
    print("\nThống kê số lượng ảnh theo từng nhãn:")
    for label, count in label_counts.items():
        print(f"Nhãn {label}: {count} ảnh")

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Cách sử dụng: python script.py <path> <keep_labels> <new_labels>")
        print("  path: Đường dẫn tới thư mục chứa 'images' và 'labels'")
        print("  keep_labels: Danh sách nhãn cần giữ lại (dạng mảng số, vd: [0,1,2])")
        print("  new_labels: Danh sách nhãn mới tương ứng (dạng mảng số, vd: [10,11,12])")
        sys.exit(1)

    input_path = sys.argv[1]
    try:
        keep_labels = eval(sys.argv[2])  # Chuyển chuỗi thành mảng
        new_labels = eval(sys.argv[3])  # Chuyển chuỗi thành mảng
    except Exception as e:
        print(f"Lỗi khi chuyển đổi tham số: {e}")
        sys.exit(1)

    process_labels(input_path, keep_labels, new_labels)