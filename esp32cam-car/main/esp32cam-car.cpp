#include "esp32cam-car.h"

static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;
static const bool esp_spp_enable_l2cap_ertm = true;

#define SPP_TAG "SPP_ACCEPTOR_DEMO"
#define SPP_SERVER_NAME "SPP_SERVER"

static const char local_device_name[] = "ESP32 CAM SPP";

static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_AUTHENTICATE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;

static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param)
{
     char bda_str[18] = {0};

     switch (event)
     {
     case ESP_SPP_INIT_EVT:
          if (param->init.status == ESP_SPP_SUCCESS)
          {
               ESP_LOGI(SPP_TAG, "ESP_SPP_INIT_EVT");
               esp_spp_start_srv(sec_mask, role_slave, 0, SPP_SERVER_NAME);
          }
          else
          {
               ESP_LOGE(SPP_TAG, "ESP_SPP_INIT_EVT status:%d", param->init.status);
          }
          break;
     case ESP_SPP_DISCOVERY_COMP_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_DISCOVERY_COMP_EVT");
          break;
     case ESP_SPP_OPEN_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_OPEN_EVT");
          break;
     case ESP_SPP_CLOSE_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_CLOSE_EVT status:%d handle:%" PRIu32 " close_by_remote:%d", param->close.status,
                   param->close.handle, param->close.async);
          break;
     case ESP_SPP_START_EVT:
          if (param->start.status == ESP_SPP_SUCCESS)
          {
               ESP_LOGI(SPP_TAG, "ESP_SPP_START_EVT handle:%" PRIu32 " sec_id:%d scn:%d", param->start.handle, param->start.sec_id,
                        param->start.scn);
               esp_bt_gap_set_device_name(local_device_name);
               esp_bt_gap_set_scan_mode(ESP_BT_CONNECTABLE, ESP_BT_GENERAL_DISCOVERABLE);
          }
          else
          {
               ESP_LOGE(SPP_TAG, "ESP_SPP_START_EVT status:%d", param->start.status);
          }
          break;
     case ESP_SPP_CL_INIT_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_CL_INIT_EVT");
          break;
     case ESP_SPP_DATA_IND_EVT:
          /*
           * We only show the data in which the data length is less than 128 here. If you want to print the data and
           * the data rate is high, it is strongly recommended to process them in other lower priority application task
           * rather than in this callback directly. Since the printing takes too much time, it may stuck the Bluetooth
           * stack and also have a effect on the throughput!
           */
          ESP_LOGI(SPP_TAG, "ESP_SPP_DATA_IND_EVT len:%d handle:%" PRIu32,
                   param->data_ind.len, param->data_ind.handle);
          if (param->data_ind.len < 128)
          {
               char *data = (char *)malloc(param->data_ind.len + 1);
               snprintf(data, param->data_ind.len + 1, "%s", param->data_ind.data);

               ESP_LOGI("DATA", "%s", data);
               free(data);
          }
          break;
     case ESP_SPP_CONG_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_CONG_EVT");
          break;
     case ESP_SPP_WRITE_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_WRITE_EVT");
          break;
     case ESP_SPP_SRV_OPEN_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_SRV_OPEN_EVT status:%d handle:%" PRIu32 ", rem_bda:[%s]", param->srv_open.status,
                   param->srv_open.handle, bda2str(param->srv_open.rem_bda, bda_str, sizeof(bda_str)));
          break;
     case ESP_SPP_SRV_STOP_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_SRV_STOP_EVT");
          break;
     case ESP_SPP_UNINIT_EVT:
          ESP_LOGI(SPP_TAG, "ESP_SPP_UNINIT_EVT");
          break;
     default:
          break;
     }
}

void esp_bt_gap_cb(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param)
{
     char bda_str[18] = {0};

     switch (event)
     {
     case ESP_BT_GAP_AUTH_CMPL_EVT:
     {
          if (param->auth_cmpl.stat == ESP_BT_STATUS_SUCCESS)
          {
               ESP_LOGI(SPP_TAG, "authentication success: %s bda:[%s]", param->auth_cmpl.device_name,
                        bda2str(param->auth_cmpl.bda, bda_str, sizeof(bda_str)));
          }
          break;
     }
     case ESP_BT_GAP_PIN_REQ_EVT:
     {
          ESP_LOGI(SPP_TAG, "Input pin code: 0000");
          esp_bt_pin_code_t pin_code = {'0', '0', '0', '0'};
          esp_bt_gap_pin_reply(param->pin_req.bda, true, 4, pin_code);
          break;
     }

     default:
     {
          ESP_LOGI(SPP_TAG, "event: %d", event);
          break;
     }
     }
     return;
}

static char *bda2str(uint8_t *bda, char *str, size_t size)
{
     if (bda == NULL || str == NULL || size < 18)
          return NULL;

     uint8_t *p = bda;
     sprintf(str, "%02x:%02x:%02x:%02x:%02x:%02x",
             p[0], p[1], p[2], p[3], p[4], p[5]);

     return str;
}

extern "C" void app_main()
{
     try
     {
          esp_err_t ret = nvs_flash_init();
          if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
          {
               if (nvs_flash_erase() != ESP_OK)
                    throw std::runtime_error("nvs_flash_erase failed");
               if (nvs_flash_init() != ESP_OK)
                    throw std::runtime_error("nvs_flash_init failed");
          }

          // Release memory reserved for classic BT controller and Bluedroid
          if (esp_bt_controller_mem_release(ESP_BT_MODE_BLE) != ESP_OK)
               throw std::runtime_error("esp_bt_controller_mem_release failed");

          // Init Bluetooth controller
          esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
          if (esp_bt_controller_init(&bt_cfg) != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" initialize controller failed") + esp_err_to_name(ret));

          // Enable Bluetooth controller
          if (esp_bt_controller_enable(ESP_BT_MODE_BTDM) != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" enable controller failed") + esp_err_to_name(ret));

          // Initialize Bluedroid
          esp_bluedroid_config_t bluedroid_cfg = BT_BLUEDROID_INIT_CONFIG_DEFAULT();
          bluedroid_cfg.ssp_en = false;

          if (esp_bluedroid_init_with_cfg(&bluedroid_cfg) != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" initialize bluedroid failed: ") + esp_err_to_name(ret));

          // Enable Bluedroid
          if (esp_bluedroid_enable() != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" enable bluedroid failed: ") + esp_err_to_name(ret));

          // Register GAP callback function
          if (esp_bt_gap_register_callback(esp_bt_gap_cb) != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" gap register failed: ") + esp_err_to_name(ret));

          // Register SPP callback function
          if (esp_spp_register_callback(esp_spp_cb) != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" spp register failed: ") + esp_err_to_name(ret));

          // Initialize SPP
          esp_spp_cfg_t bt_spp_cfg = {
              .mode = esp_spp_mode,
              .enable_l2cap_ertm = esp_spp_enable_l2cap_ertm,
              .tx_buffer_size = 0, /* Only used for ESP_SPP_MODE_VFS mode */
          };
          if (esp_spp_enhanced_init(&bt_spp_cfg) != ESP_OK)
               throw std::runtime_error(__func__ + std::string(" spp init failed: ") + esp_err_to_name(ret));

          /*
           * Set default parameters for Legacy Pairing
           * Use variable pin, input pin code when pairing
           */
          esp_bt_pin_type_t pin_type = ESP_BT_PIN_TYPE_VARIABLE;
          esp_bt_pin_code_t pin_code = {'1', '1', '1', '1'};
          esp_bt_gap_set_pin(pin_type, 4, pin_code);

          char bda_str[18] = {0};
          ESP_LOGI(SPP_TAG, "Own address:[%s]", bda2str((uint8_t *)esp_bt_dev_get_address(), bda_str, sizeof(bda_str)));
     }
     catch (const std::runtime_error &e)
     {
          ESP_LOGE(SPP_TAG, "%s", e.what());
          return;
     }
     catch (...)
     {
          ESP_LOGE(SPP_TAG, "Unknown error");
          return;
     }
}
