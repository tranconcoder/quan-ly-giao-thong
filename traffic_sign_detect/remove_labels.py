import os
import sys

def sync_images_and_labels(directory_path):
    """
    Xóa các file trong thư mục labels nếu không có file trùng tên trong thư mục images.

    Args:
        directory_path (str): Đường dẫn đến thư mục chứa hai thư mục 'images' và 'labels'.
    """
    # Đường dẫn tới các thư mục
    images_dir = os.path.join(directory_path, 'images')
    labels_dir = os.path.join(directory_path, 'labels')

    # Kiểm tra thư mục tồn tại
    if not os.path.exists(images_dir) or not os.path.exists(labels_dir):
        print("Thư mục 'images' hoặc 'labels' không tồn tại.")
        return

    # Lấy danh sách tên file (không bao gồm phần mở rộng)
    image_files = {os.path.splitext(file)[0] for file in os.listdir(images_dir) if os.path.isfile(os.path.join(images_dir, file))}
    label_files = {os.path.splitext(file)[0] for file in os.listdir(labels_dir) if os.path.isfile(os.path.join(labels_dir, file))}

    # Duyệt qua các file trong labels
    for label_file in os.listdir(labels_dir):
        label_name, _ = os.path.splitext(label_file)
        if label_name not in image_files:
            # Xóa file nếu không có tên trùng trong thư mục images
            label_path = os.path.join(labels_dir, label_file)
            os.remove(label_path)
            print(f"Đã xóa: {label_path}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <directory_path>")
    else:
        sync_images_and_labels(sys.argv[1])

