@echo off
mkdir target
mkdir target\%1
node capture.js --url http://localhost:8080/ 											--file target/%1/1_1_intro.png 			--viewportWidth %1
node capture.js --url http://localhost:8080/content/admin.html 									--file target/%1/1_2_admin.png 			--viewportWidth %1

node capture.js --url http://localhost:8080/content/admin/pages.html 								--file target/%1/2_1_pages.png 			--viewportWidth %1
node capture.js --url http://localhost:8080/content/admin/pages/edit.html/path///content/sites/example 				--file target/%1/2_2_edit.png 			--viewportWidth %1
node capture.js --url http://localhost:8080/content/admin/pages/create.html/path///content/sites/example 			--file target/%1/2_3_pages-create.png 		--viewportWidth %1

node capture.js --url http://localhost:8080/content/admin/assets.html 								--file target/%1/3_1_assets.png 		--viewportWidth %1

node capture.js --url http://localhost:8080/content/admin/objects.html 								--file target/%1/4_1_objects.png 		--viewportWidth %1
