FROM python:3.14.7
RUN mkdir /experiment
WORKDIR /experiment
COPY .python-version ./
COPY plotting plotting
COPY requirements.txt ./
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install --no-cache-dir seaborn matplotlib pandas
CMD ["python", "plotting/charts_generator.py"]
