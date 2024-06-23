cd /dev-ops
docker-compose -f docker-compose-environment.yml up -d

cd /dev-ops/
mkdir "github"

## 构建后端镜像
cd /dev-ops/github/
git clone https://github.com/Mythhhhhhhh/BigMarket.git

cd BigMarket/

git checkout -b 13-dev-ops-tag-raffle

mvn clean install

cd BigMarket-app/

ls

chmod +x build.sh

./build.sh

## 构建前端镜像
cd /dev-ops/github/
git clone https://github.com/Mythhhhhhhh/BigMarket-front.git

git checkout -b 04-dev-ops-tag-raffle
240217-xfg-dev-ops-tag-raffle

cd big-market-front

chmod +x build.sh

./build.sh

